import {Component, OnInit} from '@angular/core';
import {ImagineService} from '@shared/modules/imagine/imagine.service';
import {EntityType} from '@shared/domain/entities';
import {FileItem, FileUrl} from '@shared/modules/imagine/file-item';
import {NGXLogger} from 'ngx-logger';
import {AudioService, StreamState} from '@app/radio/audio.service';
import {debounceTime, distinctUntilChanged, filter, map, startWith, takeUntil} from 'rxjs/operators';
import {FormControl} from '@angular/forms';
import {combineLatest, Observable} from 'rxjs';
import {WithDestroy} from '@shared/mixins/with-destroy';
import {NotificationService} from '@shared/services/notification.service';

/**
 * Radio Component inspired by https://github.com/imsingh/auth0-audio
 * Inspiration for list Filter:
 *
 * https://stackblitz.com/edit/angular-filtering-rxjs-3wfwny
 * https://blog.angulartraining.com/dynamic-filtering-with-rxjs-and-angular-forms-a-tutorial-6daa3c44076a
 */
@Component({
  selector: 'app-song',
  templateUrl: './radio.component.html',
  styleUrls: ['./radio.component.scss']
})
export class RadioComponent extends WithDestroy() implements OnInit {

  private readonly className = 'RadioComponent';

  //songs: FileItem[] = [];
  state: StreamState;
  songs$: Observable<FileItem[]>;
  filteredSongs$: Observable<FileItem[]>;
  filterCtl: FormControl;
  filter$: Observable<string>;
  currentSong: { index?: number, song?: FileItem } = {};
  currentPlaylist: FileItem[];

  constructor(private imagineService: ImagineService,
              private logger: NGXLogger,
              private audioService: AudioService,
              private notifier: NotificationService,
  ) {
    super(); // required for WithDestroy() mixin
  }

  /** Load songs, listen to current stream state */
  ngOnInit(): void {
    this.filterCtl = new FormControl('');
    this.filter$ = this.filterCtl.valueChanges.pipe(startWith(''));

    // Load initial song list from imagine
    this.songs$ = this.imagineService
      .getEntityFiles(EntityType.Song)
      .pipe(
        // Simple filter on array of RXJS Observable: https://stackoverflow.com/a/43219046/4292075
        map<FileItem[], FileItem[]>(items =>
          items
            .filter(item => item.filename.endsWith('.mp3'))
            .sort((a, b) => a.path.localeCompare(b.path))
        ),
      );

    this.filteredSongs$ = combineLatest([this.songs$, this.filter$])
      .pipe(
        map(([songs, filterString]) => songs.filter(song => song.path.toLowerCase().indexOf(filterString.toLowerCase()) !== -1))
      );

    // Listen to stream state, subscription 1: only update current state for time display etc.
    this.audioService.getState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(streamState => this.state = streamState);

    // Listen to stream state, subscription #2: trigger next songs if one song is finished, and songs are left to play.
    // Stream events emit by audio service look as follows:
    // { "playing":true,"readableCurrentTime":"02:36","readableDuration":"02:36",
    //  "duration":156.656327,"currentTime":156.685584,"canplay":true,"error":false }
    this.audioService.getState()
      // it seems we get the same event twice here, so we debounce it to call next() only once for auto-forward
      .pipe(
        filter(streamState => ! streamState.playing), // act only if we stopped playing
        debounceTime(500),
        // would be a more rxjs style way to do prevent duplicate events, but didn't work
        // distinctUntilChanged((prev, curr) => {
        //   return (
        //     prev.currentTime === curr.currentTime
        //   )}),
        takeUntil(this.destroy$),
      )
      .subscribe(streamState => {
        this.logger.debug(`${this.className}: next one check event ${JSON.stringify(streamState)}`);
        // if a playlist ist active, a song reached the end of its duration and stopped playing,
        // we can auto-forward it the list is not exhausted yet
        // check if the song reached the end: streamState.readableCurrentTime == streamState.readableDuration
        if (this.currentPlaylist && streamState.currentTime >= streamState.duration ) {
          this.logger.debug(`${this.className}: song ${this.currentSong.index} finished playing at ${streamState.readableCurrentTime}`);
          if (this.currentSong.index < this.currentPlaylist.length - 1) {
            this.logger.debug(`${this.className}: next one please`);
            this.notifier.info(`Now playing: ${this.currentPlaylist[this.currentSong.index+1].tags['Title']}`)
            this.next(); // auto-forward to next title
          } else {
            this.logger.debug(`${this.className}: no more songs to play, at end of playlist`);
          }
        } // end outer if
      });
  }

  /** Uses the AudioService to play the current stream */
  playStream(url) {
    this.audioService.playStream(url)
      .pipe(takeUntil(this.destroy$))
      // too many events to display them here with no meaningful content, observe only errors
      .subscribe({error: error => this.logger.error(`playStream(): ${error}`)});
  }

  openSong(playlist: FileItem[], index) {
    this.currentPlaylist = playlist;
    const song = this.currentPlaylist[index];
    this.logger.debug(`${this.className}: Obtaining presignedUrl for ${song.path}`);
    this.imagineService.getPresignUrl(song.path)
      .subscribe(r => {
        const fileUrl = r as FileUrl; // todo should be already returned as FileUrl
        this.currentSong = {index, song};
        this.audioService.stop();
        this.playStream(fileUrl.url);
        // window.open(fileUrl.url, "_song")
      });

  }

  pause() {
    this.audioService.pause();
  }

  play() {
    this.audioService.play();
  }

  stop() {
    this.audioService.stop();
  }

  next() {
    const current = this.currentSong.index;
    const index = current < (this.currentPlaylist.length - 1) ? current + 1 : current;
    this.openSong(this.currentPlaylist, index);
  }

  previous() {
    const current = this.currentSong.index;
    const index = current >= 0 ? this.currentSong.index - 1 : 0;
    this.openSong(this.currentPlaylist, index);
  }

  /*
  isFirstPlaying() {
    return this.currentSong.index && this.currentSong.index === 0;
  }

  isLastPlaying() {
    this.logger.debug(this.currentSong?.index + " vs " + this.currentPlaylist?.length)
    return this.currentSong.index && this.currentSong.index === this.currentPlaylist.length ;
  }
   */

  onSliderChangeEnd(change) {
    this.audioService.seekTo(change.value);
  }

}


// {
//   "filename": "imagine/songs/01 - Mein Lied.mp3",
//   "path": "/imagine/songs/01 - Mein Lied.mp3",
//   "tags": {
//     "Album": "Fascination Space",
//     "Artist": "Drive in Vacation Racing Team",
//     "ContentType": "audio/mpeg",
//     "Genre": "Rock",
//     "Origin": "multipart/form-data",
//     "Size": "5743463",
//     "Title": "Mein Lied",
//     "Track": "1/15",
//     "Year": "2014"
//   }

