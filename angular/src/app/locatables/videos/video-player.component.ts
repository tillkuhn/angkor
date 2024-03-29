import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {AuthService} from '@shared/services/auth.service';
import {ActivatedRoute} from '@angular/router';
import {VideoStoreService} from '@app/locatables/videos/video-store.service';
import {Video} from '@domain/location';

// https://stackblitz.com/edit/youtube-player-demo
@Component({
  selector: 'app-youtube-player-demo',
  templateUrl: 'video-player.component.html',
  styleUrls: ['video-player.component.scss', '../../shared/components/common.component.scss'],
})
export class VideoPlayerComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('youTubePlayer') youTubePlayer: ElementRef<HTMLDivElement>;
  playerWidth: number | undefined;
  playerHeight: number | undefined;
  playerApiLoaded = false;

  item: Video;
  private readonly className = 'VideoPlayerComponent';

  constructor(public authService: AuthService,
              public videoService: VideoStoreService,
              private changeDetectorRef: ChangeDetectorRef,
              private route: ActivatedRoute,
              private logger: NGXLogger) {
  }

  ngOnInit(): void {
    // Load IFrame Player API on demand
    if (!this.playerApiLoaded) {
      // This code loads the IFrame Player API code asynchronously, according to the instructions at
      // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
      this.logger.info(`${this.className}.onInit: Loading Youtube API`);
      const tag = document.createElement('script');
      tag.src = 'https://www.youtube.com/iframe_api';
      document.body.appendChild(tag);
      this.playerApiLoaded = true;
    }

    if (this.route.snapshot.paramMap.has('id')) {
      const id = this.route.snapshot.paramMap.get('id');
      this.logger.debug(`Id param found, zoom in on video ${id}`);
      this.videoService.getItem(id).subscribe((data: Video) => {
          this.item = data;
          // this.logger.debug('getItem()', this.item);
        });
    } else {
      this.logger.error('Expected param id');
    }

  }

  // Player Resize
  onResize = (): void => {
    // Automatically expand the video to fit the page up to 1200px x 720px
    this.playerWidth = Math.min(this.youTubePlayer.nativeElement.clientWidth, 1280);
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


  /*
  this.linkService.getVideo$()
    .subscribe(videos => {
      this.availableOptions = videos;

   */
  // If called with id (e.g. /videos/12345-123..., focus on this

  /*
  // register change listener for input control to recalculate choices
  this.filteredOptions = this.optionInputCtrl.valueChanges
    .pipe(
      startWith<string | Link>(''),
      map(value => typeof value === 'string' ? value : value?.name),
      map(name => name ? this.filterOptions(name) : this.availableOptions.slice())
    );
});
   */

  // https://stackblitz.com/edit/mat-autcomplete-displayfn?file=app%2Fautocomplete-display-example.ts
  // optionInputCtrl = new FormControl(); // mapped to the input's formControl

  // https://material.angular.io/components/autocomplete/examples
  // filteredOptions: Observable<Link[]>; // passed as filteredOptions | async to mat-option element (ngFor)
  // availableOptions: Link[]; // all options to select from
  //  selectedOption: Link | undefined; // set by optionSelectedEvent inside mat-autocomplete

  // displayWithFunction for autocomplete
  /*
  getVideoName(selectedOption: Link): string {
    if (this.availableOptions?.length > 0 && selectedOption != null) {
      return this.availableOptions.find(video => video.id === selectedOption.id).name;
    } else {
      return '';
    }
  }
   */

  /*
  clearInput() {
    this.optionInputCtrl.setValue(null); // field contains an object, so we reset to null, not ''
  }
   */

  // force reload video list
  /*
  refreshOptions(): void {
    this.ngOnInit();
  }
   */


  // Dialogs

  /*
  openEditDialog(): void {
    this.openDetailsDialog(this.selectedOption);
  }
  openAddDialog(): void {
    this.openDetailsDialog({mediaType: 'VIDEO'}); // Initialized new video
  }

  openAddTourDialog(): void {
    this.openDetailsDialog({mediaType: 'KOMOOT_TOUR'}); // Initialized new video
  }

  openDeleteDialog(): void {
    this.notifications.warn('Not yet implemented, coming soon 😊');
  }

  // Input new Video
  openDetailsDialog(data: any): void {
    const dialogRef = this.dialog.open(LinkDetailsComponent, {
      // width: '75%',
      // maxWidth: '600px',
      // Defined centrally in styles.scss (with .mat-dialog-container)
      panelClass: 'app-details-panel',
      data
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const link = result as Link;
        this.logger.debug(`${this.className}.dialogRef.afterClosed: store result=${link.linkUrl}`);
        this.linkService.addOrUpdateItem(link).subscribe(newLink => {
          this.logger.debug(`${this.className}.DetailsDialog: New Links is save - id=${newLink.id} url=${newLink.linkUrl} `);
          this.refreshOptions();
        });
      } else {
        this.logger.debug('${this.className}.dialogRef.afterClosed: dialog was cancelled');
      }
    });
  }

  private filterOptions(name: string): Link[] {
    // const filterValue = (typeof name === 'string') ?  name.toLowerCase() : name.name.toLowerCase();
    const filterValue = name.toLowerCase();
    // === 0 is starts with, >= 0 is contains
    return this.availableOptions.filter(video => video.name.toLowerCase().indexOf(filterValue) >= 0);
  }
   */

}
