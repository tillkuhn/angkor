import {Component, OnInit} from '@angular/core';
import {ImagineService} from '@shared/modules/imagine/imagine.service';
import {EntityType} from '@shared/domain/entities';
import {FileItem, FileUrl} from '@shared/modules/imagine/file-item';
import {NGXLogger} from 'ngx-logger';
import {AudioService, StreamState} from '@app/radio/audio.service';
import {map} from 'rxjs/operators';

/**
 * Radio Component inspired by https://github.com/imsingh/auth0-audio
 */
@Component({
  selector: 'app-song',
  templateUrl: './radio.component.html',
  styleUrls: ['./radio.component.scss']
})
export class RadioComponent implements OnInit {

  private readonly className = 'RadioComponent';

  songs: FileItem[] = [];
  state: StreamState;
  currentFile: any = {};

  constructor(private imagineService: ImagineService,
              private logger: NGXLogger,
              private audioService: AudioService,
  ) {
  }

  /** Load songs, listen to current stream state */
  ngOnInit(): void {
    // Load songs
    // https://stackoverflow.com/a/43219046/4292075
    this.imagineService
      .getEntityFiles(EntityType.Song)
      .pipe(
        map<FileItem[], FileItem[]>(items =>
          items
            .filter(item => item.filename.endsWith('.mp3'))
            .sort((a,b) => a.path.localeCompare(b.path))
        )
      )
      .subscribe(res => {
        this.songs = res;
        this.logger.debug(`${this.className}.loadFiles: ${this.songs ? this.songs.length : 0}`);
      }); // end subscription

    // Listen to stream state
    this.audioService.getState()
      .subscribe(state => {
        this.state = state;
      });
  }

  playStream(url) {
    this.audioService.playStream(url)
      .subscribe(_ => {
        // too many events to display them here ...
        // this.logger.debug(`${this.className}.playStream: ${events}`)
      });
  }

  openSong(song: FileItem, index) {
    this.logger.debug(`Obtaining presignedUrl for ${song.path}`);
    this.imagineService.getPresignUrl(song.path)
      .subscribe(r => {
        const fileUrl = r as FileUrl; // todo should be already returned as FileUrl
        this.currentFile = {index, song};
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
    const index = this.currentFile.index + 1;
    const file = this.songs[index];
    this.openSong(file, index);
  }

  previous() {
    const index = this.currentFile.index - 1;
    const file = this.songs[index];
    this.openSong(file, index);
  }

  isFirstPlaying() {
    return this.currentFile.index === 0;
  }

  isLastPlaying() {
    return this.currentFile.index === this.songs.length - 1;
  }

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

