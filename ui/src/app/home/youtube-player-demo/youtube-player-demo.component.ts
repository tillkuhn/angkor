import {
  AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild
} from '@angular/core';
import {NGXLogger} from 'ngx-logger';

interface Video {
  id: string;
  name: string;
}

const VIDEOS: Video[] = [
  {
    id: '11cA8h2YAZQ',
    name: 'Devil\'s Tears on Nusa Lembongan',
  },
  {
    id: 'nScz_nNwUl8',
    name: 'Fireshow @ Ume Cafe, Ngwe Saung',
  },
  {
    id: 'PBlrX41ot7c',
    name: 'Flower Power @Spring River Kong Lor',
  },
  {
    id: 'S8kvEf50Xvo',
    name: 'Sabaidee Pi Mai Lao',
  }
];


// https://stackblitz.com/edit/youtube-player-demo
@Component({
  selector: 'app-youtube-player-demo',
  templateUrl: 'youtube-player-demo.component.html',
  styleUrls: ['youtube-player-demo.component.scss'],
})
export class YoutubePlayerDemoComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('demoYouTubePlayer') demoYouTubePlayer: ElementRef<HTMLDivElement>;
  selectedVideo: Video | undefined = VIDEOS[0];
  videos = VIDEOS;
  videoWidth: number | undefined;
  videoHeight: number | undefined;
  apiLoaded = false;

  constructor(private changeDetectorRef: ChangeDetectorRef,
              private logger: NGXLogger) {}

  ngOnInit(): void {
    if (!this.apiLoaded) {
      // This code loads the IFrame Player API code asynchronously, according to the instructions at
      // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
      this.logger.info('Loading API')
      const tag = document.createElement('script');
      tag.src = 'https://www.youtube.com/iframe_api';
      document.body.appendChild(tag);
      this.apiLoaded = true;
    } else {
      this.logger.info('API Loaded');
    }
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
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.onResize);
  }
}
