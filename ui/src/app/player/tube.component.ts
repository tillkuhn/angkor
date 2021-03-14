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
  // https://stackblitz.com/edit/mat-autcomplete-displayfn?file=app%2Fautocomplete-display-example.ts
  optionInputCtrl = new FormControl(); // mapped to the input's formControl
  filteredOptions: Observable<Video[]>; // passed as filteredOptions | async to mat-option element (ngFor)
  availableOptions: Video[]; // all options to select from
  selectedOption: Video | undefined; // set by optionSelectedEvent inside mat-autocomplete

  @ViewChild('demoYouTubePlayer') demoYouTubePlayer: ElementRef<HTMLDivElement>;
  playerWidth: number | undefined;
  playerHeight: number | undefined;
  playerApiLoaded = false;

  constructor(public videoService: VideoService,
              private changeDetectorRef: ChangeDetectorRef,
              private logger: NGXLogger) {
  }

  ngOnInit(): void {
    // Load IFrame Player API on demand
    if (!this.playerApiLoaded) {
      // This code loads the IFrame Player API code asynchronously, according to the instructions at
      // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
      this.logger.info('TubeComponent.ngOnInit: Loading Youtube API');
      const tag = document.createElement('script');
      tag.src = 'https://www.youtube.com/iframe_api';
      document.body.appendChild(tag);
      this.playerApiLoaded = true;
    }


    this.videoService.getVideo$()
      .subscribe( videos => {
        this.availableOptions = videos;
        // register change listener for input control to recalculate choices
        this.filteredOptions = this.optionInputCtrl.valueChanges
          .pipe(
            startWith<string| Video>(''),
            map(value => typeof value === 'string' ? value : value?.name),
            map(name => name ? this.filterOptions(name) : this.availableOptions.slice())
          );
      });
   }

   // displayWithFunction for autocomplete
  getVideoName(selectedOption: Video): string {
    // this.logger.info('getVideoName', selectedOption); // could be null if field is cleared
    if (this.availableOptions?.length > 0 && selectedOption != null) {
      return this.availableOptions.find(video => video.id === selectedOption.id).name;
    } else {
      return '';
    }
  }

  clearInput() {
    // this.logger.info(this.videoInputCtrl.value);
    this.optionInputCtrl.setValue(null); // field contains an object, so we reset to null, not ''
  }

  // force reload video list
  refreshOptions(): void {
    this.videoService.clearCache();
    this.ngOnInit();
  }

  private filterOptions(name: string): Video[] {
   //  this.logger.info('filter by', name);
    // const filterValue = (typeof name === 'string') ?  name.toLowerCase() : name.name.toLowerCase();
    const filterValue = name.toLowerCase();
    // === 0 is starts with, >= 0 is contains
    return this.availableOptions.filter(video => video.name.toLowerCase().indexOf(filterValue) >= 0);
  }

  // for resize of player
  onResize = (): void => {
    // Automatically expand the video to fit the page up to 1200px x 720px
    this.playerWidth = Math.min(this.demoYouTubePlayer.nativeElement.clientWidth, 1280);
    this.playerHeight = this.playerWidth * 0.6;
    this.changeDetectorRef.detectChanges();
  }

  ngAfterViewInit(): void {
    this.onResize();
    window.addEventListener('resize', this.onResize);
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.onResize);
  }

}
