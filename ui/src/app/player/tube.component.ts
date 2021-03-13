import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {Video, VideoService} from './video.service';
import {Observable} from 'rxjs';
import {FormControl} from '@angular/forms';
import {map, startWith} from 'rxjs/operators';

// https://stackblitz.com/edit/youtube-player-demo
@Component({
  selector: 'app-youtube-player-demo',
  templateUrl: 'tube.component.html',
  styleUrls: ['tube.component.scss'],
})
export class TubeComponent implements OnInit, AfterViewInit, OnDestroy {

  // https://material.angular.io/components/autocomplete/examples
  videoInputCtrl = new FormControl();
  filteredVideos: Observable<Video[]>;
  availableVideos: Video[];

  @ViewChild('demoYouTubePlayer') demoYouTubePlayer: ElementRef<HTMLDivElement>;
  selectedVideo: Video | undefined;
  videoWidth: number | undefined;
  videoHeight: number | undefined;
  apiLoaded = false;

  constructor(public videoService: VideoService,
              private changeDetectorRef: ChangeDetectorRef,
              private logger: NGXLogger) {
  }

  ngOnInit(): void {
    // Load IFrame Player API on demand
    if (!this.apiLoaded) {
      // This code loads the IFrame Player API code asynchronously, according to the instructions at
      // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
      this.logger.info('TubeComponent.ngOnInit: Loading Youtube API');
      const tag = document.createElement('script');
      tag.src = 'https://www.youtube.com/iframe_api';
      document.body.appendChild(tag);
      this.apiLoaded = true;
    }

    this.videoService.getVideo$()
      .subscribe( videos => {
        this.availableVideos = videos;
        this.filteredVideos = this.videoInputCtrl.valueChanges
          .pipe(
            startWith(''),
            map(video => video ? this.filterVideos(video) : this.availableVideos.slice())
          );
      });
   }

   showVideo(event: any) {
    this.logger.info(`selected ${event}`);
   }

   //
  getVideoName(selectedVideo: Video): String {
    return this.availableVideos ? this.availableVideos.find(video => video.id === selectedVideo.id).name : '';
  }

  refresh(): void {
    this.videoService.clearCache();
    this.ngOnInit();
  }

  private filterVideos(value: string | Video): Video[] {
    // this.logger.info('filter by', value);
    const filterValue = (typeof value === 'string') ?  value.toLowerCase() : value.name.toLowerCase();
    // === 0 is starts with, >= 0 is contains
    return this.availableVideos.filter(video => video.name.toLowerCase().indexOf(filterValue) >= 0);
  }

  ngAfterViewInit(): void {
    this.onResize();
    window.addEventListener('resize', this.onResize);
  }

  onResize = (): void => {
    // Automatically expand the video to fit the page up to 1200px x 720px
    this.videoWidth = Math.min(this.demoYouTubePlayer.nativeElement.clientWidth, 1200);
    this.videoHeight = this.videoWidth * 0.6;
    this.changeDetectorRef.detectChanges();
  };

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.onResize);
  }

}
