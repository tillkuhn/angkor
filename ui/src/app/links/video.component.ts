import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';
import {FormControl} from '@angular/forms';
import {map, startWith} from 'rxjs/operators';
import {Link} from '@domain/link';
import {LinkStoreService} from './link-store.service';
import {MatDialog} from '@angular/material/dialog';
import {LinkDetailsComponent} from './details/link-details.component';
import {AuthService} from '@shared/services/auth.service';

// https://stackblitz.com/edit/youtube-player-demo
@Component({
  selector: 'app-youtube-player-demo',
  templateUrl: 'video.component.html',
  styleUrls: ['video.component.scss'],
})
export class VideoComponent implements OnInit, AfterViewInit, OnDestroy {

  private readonly className = 'VideoComponent';

  // https://material.angular.io/components/autocomplete/examples
  // https://stackblitz.com/edit/mat-autcomplete-displayfn?file=app%2Fautocomplete-display-example.ts
  optionInputCtrl = new FormControl(); // mapped to the input's formControl
  filteredOptions: Observable<Link[]>; // passed as filteredOptions | async to mat-option element (ngFor)
  availableOptions: Link[]; // all options to select from
  selectedOption: Link | undefined; // set by optionSelectedEvent inside mat-autocomplete

  @ViewChild('demoYouTubePlayer') demoYouTubePlayer: ElementRef<HTMLDivElement>;
  playerWidth: number | undefined;
  playerHeight: number | undefined;
  playerApiLoaded = false;

  constructor(public linkService: LinkStoreService,
              public authService: AuthService,
              private changeDetectorRef: ChangeDetectorRef,
              private dialog: MatDialog,
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

    this.linkService.getVideo$()
      .subscribe( videos => {
        this.availableOptions = videos;
        // register change listener for input control to recalculate choices
        this.filteredOptions = this.optionInputCtrl.valueChanges
          .pipe(
            startWith<string| Link>(''),
            map(value => typeof value === 'string' ? value : value?.name),
            map(name => name ? this.filterOptions(name) : this.availableOptions.slice())
          );
      });
   }

   // displayWithFunction for autocomplete
  getVideoName(selectedOption: Link): string {
    // this.logger.info('getVideoName', selectedOption); // could be null if field is cleared
    if (this.availableOptions?.length > 0 && selectedOption != null) {
      return this.availableOptions.find(video => video.id === selectedOption.id).name;
    } else {
      return '';
    }
  }

  clearInput() {
    this.optionInputCtrl.setValue(null); // field contains an object, so we reset to null, not ''
  }

  // force reload video list
  refreshOptions(): void {
    this.linkService.clearCache();
    this.ngOnInit();
  }

  private filterOptions(name: string): Link[] {
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

  openEditDialog(): void {
    this.openDetailsDialog(this.selectedOption);
  }

  openNewVideoDialog(): void {
    this.openDetailsDialog({mediaType: 'VIDEO'});
  }

  // Input new Video
  openDetailsDialog(data: any): void {
    const dialogRef = this.dialog.open(LinkDetailsComponent, {
      width: '350px',
      data
    });

    dialogRef.afterClosed().subscribe(result  => {
      if (result) {
        const link = result as Link;
        this.logger.debug(`${this.className}.dialogRef.afterClosed: store result=${link.linkUrl}`);
        if (link.id) {
          this.linkService.updateItem(link.id, link).subscribe(newLink => {
            this.logger.debug(`${this.className}.DetailsDialog: Got updated link ${newLink.id} + ${newLink.linkUrl} `);
            this.refreshOptions();
          });
        } else {
          this.linkService.addItem(link).subscribe(newLink => {
            this.logger.debug(`${this.className}.DetailsDialog: Got new link ${newLink.id} + ${newLink.linkUrl} `);
            this.refreshOptions();
          });
        }
      } else {
        this.logger.debug('${this.className}.dialogRef.afterClosed: dialog was cancelled');
      }
    });
  }

}
