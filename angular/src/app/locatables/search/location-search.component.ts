import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '@shared/services/auth.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {ComponentType} from '@angular/cdk/portal';
import {EntityDialogRequest, EntityDialogResponse} from '@app/locatables/entity-dialog';
import {EntityMetadata, EntityType, EntityTypeInfo} from '@shared/domain/entities';
import {ListItem} from '@shared/domain/list-item';
import {Location as AngularLocation} from '@angular/common';
import {LocationStoreService} from '@app/locatables/location-store.service';
import {Location} from '@domain/location';
import {MasterDataService} from '@shared/services/master-data.service';
import {MatDialog} from '@angular/material/dialog';
import {NGXLogger} from 'ngx-logger';
import {PhotoDetailsComponent} from '@app/locatables/photos/photo-details.component';
import {PostDetailsComponent} from '@app/locatables/posts/post-details.component';
import {Observable, Subject} from 'rxjs';
import {TourDetailsComponent} from '@app/locatables/tours/tour-details.component';
import {VideoDetailsComponent} from '@app/locatables/videos/video-details.component';
import {WithDestroy} from '@shared/mixins/with-destroy';
import {debounceTime, distinctUntilChanged, filter, map, shareReplay, switchMap, takeUntil} from 'rxjs/operators';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {ConfirmDialogComponent, ConfirmDialogModel} from '@shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-location-list',
  templateUrl: './location-search.component.html',
  styleUrls: [
    '../../shared/components/chip-list/chip-list.component.scss', // so you can use coloured chip classes
    '../../shared/components/common.component.scss', // for mat elevation etc.
    './location-search.component.scss' // dedicated for this component
  ]
})
export class LocationSearchComponent extends WithDestroy() implements OnDestroy, OnInit {

  private readonly className = 'LocationSearchComponent';

  readonly entityTypes: Array<EntityTypeInfo> = [
    EntityMetadata[EntityType.Place],
    EntityMetadata[EntityType.Tour],
    EntityMetadata[EntityType.Video],
    EntityMetadata[EntityType.Post],
    EntityMetadata[EntityType.Photo],
  ];

  /** toggle/hide properties for advanced search */
  toggleShowHide = false;
  sortProperties: ListItem[] = [
    {value: 'name', label: 'Name'},
    {value: 'areaCode', label: 'Region'},
    // {value: 'locationType', label: 'Type'},
    {value: 'updatedAt', label: 'Updated'},
    {value: 'authScope', label: 'Authscope'}
  ];

  entityType: EntityType; // set by ngInit based on route data
  items: Location[] = [];
  keyUp$ = new Subject<string>();
  minSearchTermLength = 1; // min number of keyed in chars to trigger a search

  isHandset$: Observable<boolean> = this.breakpointObserver
    .observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay(),
      takeUntil(this.destroy$),
    );

  constructor(
    public authService: AuthService,
    private breakpointObserver: BreakpointObserver, // Utility for checking the matching state of @media queries.
    public masterData: MasterDataService,
    public store: LocationStoreService,
    private dialog: MatDialog,
    private location: AngularLocation, // Alias for Location, a service that applications can use to interact with a browser's URL.
    private logger: NGXLogger,
    private route: ActivatedRoute, // provides access to info about a route associated with a component that is loaded in an outlet.
    private router: Router,
  ) {
    super();
  }

  /** get called once when the locatable search component is initialized */
  ngOnInit(): void {

    // Get router data, only works for components that don't navigate: https://stackoverflow.com/a/46697826/4292075
    this.entityType = this.route.snapshot.data.entityType;
    this.logger.info(`${this.className}.ngOnInit(): Warming up for entityType=${this.entityType} path=${this.location.path()}`);

    this.store.searchRequest.primarySortProperty = 'updatedAt';
    this.store.searchRequest.sortDirection = 'DESC';
    this.store.searchRequest.entityTypes = [this.entityType];

    this.keyUp$.pipe(
      filter(term => term.length >= this.minSearchTermLength),
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(() => this.store.searchItems()),
      takeUntil(this.destroy$), // avoid leak https://stackoverflow.com/a/41177163/4292075 (take this.destroy$ from mixin)
    ).subscribe({
      next: items => this.items = items,
      error: () => {
      },
      complete: () => this.logger.info(`${this.className}.ngOnInit(): Search completed`)
    });

    // if called with id (e.g. /videos/12345), open details panel (deeplink)
    if (this.route.snapshot.params?.id) {
      const detailsId = this.route.snapshot.params.id;
      this.logger.debug(`${this.className}.ngOnInit(): Deeplink for id ${detailsId}, invoke dialog`);
      this.openDetailsDialog(detailsId, this.entityType);
      // else run initial search on page load, comment out if you want to have the search triggered by user interaction
    } else {
      this.runSearch();
    }
  }

  /** triggers a new search with current search params */
  runSearch() {
    this.store.searchItems().subscribe(items => this.items = items);
  }

  /** onEntityTypesChange is triggered when the user selects a different type */
  onEntityTypesChange(entry: { [key: string]: any }) {
    this.logger.info(`${this.className} Switch to entityType Filter=${entry.id}`);
    // TODO Support Multi Entity Search (MESs :-))
    // todo this is a bit redundant
    this.entityType = EntityType[entry.id];
    this.store.searchRequest.entityTypes = [entry.id];
    this.location.go(`/${EntityMetadata[entry.id].path}`); // Adapt URL to new path
    this.runSearch();
  }

  /** returns a suggestion for a minified preview url */
  previewImageUrl(item: Location) {
    if (!item.imageUrl) {
      return EntityMetadata[item.entityType].iconUrl;
      // See videos/README.adoc replace high-res image with small (default.jpg) 120px image to save bandwidth
    } else if (item.imageUrl.toLowerCase().startsWith('https://img.youtube.com/')) {
      return item.imageUrl.replace('/sddefault.jpg', '/default.jpg');
      // example /imagine/places/a515f07b-2871-4d62-ad6d-d5109545279d/view_mini.jpg?large
    } else if (item.imageUrl.startsWith('/imagine/')) {
      return item.imageUrl.replace('?large', '?small');
    } else {
      return item.imageUrl;
    }
  }

  /**
   * Router link for action (e.g. play video)
   */
  routerLink(item: Location) {
    let path: string;
    switch (item.entityType) {
      case EntityType.Video:
        path = `/player/${item.id}`;
        break;
      default:
        this.logger.trace(`${item.entityType} router link not yet supported`);
    }
    return path;
  }

  /** addDetailsDialog opens a new Add / Import dialog if supported by the entity Type **/
  addDetailsDialog( ): void  {
    switch (this.entityType) {
      case EntityType.Place:
        this.logger.debug(`EntityType ${this.entityType} Invoke Add Dialogue`);
        this.router.navigate([`/places/add`]).then(); // swallow returned promise
        return;
      case EntityType.Tour:
        const dialogData = new ConfirmDialogModel('Confirm Action', "Import Coming soon, please be patient!");
        const dialogRef = this.dialog.open(ConfirmDialogComponent, {maxWidth: '400px', data: dialogData,});
        dialogRef.afterClosed().subscribe(dialogResult => {
          if (dialogResult) {
            this.logger.debug('message acknowledged');
          }
        });
        return;
      default:
        throw new Error(`Current entityType ${this.entityType} not yet supported in this component`);
    }
  }

  /** addDetailsSupported returns true if the current entityType supports add/import functionality */
  addDetailsSupported(): boolean {
    return EntityType.Place === this.entityType || EntityType.Tour === this.entityType;
  }


  /**
   *   Open Details Modal Panel
   *   delegate to entity specific component which loads the entity by id
   *   rowIndex is used to update the list element once the dialog is closed (if it has been updated)
   */
  openDetailsDialog(id: string, entityType: EntityType, rowIndex: number = -1): void {
    // append id to location path (unless it's already there)
    // so we can bookmark (see notes.component.ts)
    // Todo since location search allows to change the initial entityType, we must also change the entityType path in the
    // URL (e.g. /tours/123345 /videos/123345 if the initial call to search went to tours
    const locationPathBeforeOpen = this.location.path();
    if (locationPathBeforeOpen.indexOf(id) < 0) {
      // Changes the browser's URL to a normalized version of a given URL, and pushes a new item onto the platform's history.
      this.location.go(`${locationPathBeforeOpen}/${id}`);
    }

    let componentClass: ComponentType<unknown>;
    switch (entityType) {
      case EntityType.Video:
        componentClass = VideoDetailsComponent;
        break;
      case EntityType.Photo:
        componentClass = PhotoDetailsComponent;
        break;
      case EntityType.Tour:
        componentClass = TourDetailsComponent;
        break;
      case EntityType.Post:
        componentClass = PostDetailsComponent;
        break;
      case EntityType.Place:
        // componentClass = PostDetailsComponent;
        this.logger.warn(`EntityType ${entityType} Special Temporary handling reroute to details`);
        this.router.navigate([`/places/details`, id]).then(); // swallow returned promise
        return;
      // break;
      default:
        throw new Error(`EntityType ${entityType} not yet supported in this component`);
    }
    const dialogRequest: EntityDialogRequest = {
      id, // object shorthand literal (id: id)
      mode: 'View',
    };
    const dialogRef = this.dialog.open(componentClass, {
      // width: '75%', maxWidth: '600px',
      // dims etc. now defined centrally in styles.scss (with .mat-dialog-container)
      panelClass: 'app-details-panel',
      data: dialogRequest,
    });

    // Callback when the dialog is closed, most importantly to have list elements reflect the changes immediately
    dialogRef.afterClosed().subscribe((response: EntityDialogResponse<Location>) => {
      this.location.go(locationPathBeforeOpen); // restore previous path (the one w/o id)
      this.logger.debug(`${this.className}.dialogRef.afterClosed: result=${response.result} updItem=${response.entity?.name}`);
      switch (response.result) {
        case 'Updated':
          if (!response.entity) {
            this.logger.warn(`${this.className}: no entity returned, cannot update list`);
          } else {
            this.items[rowIndex] = response.entity; // updated row in current list
          }
          break;
        case 'Deleted':
          if (rowIndex > -1) {
            this.items.splice(rowIndex, 1);
          }
          break;
      }
    });

  }


  // Make sure to include shared/components/chip-list/chip-list.component.scss'
  // shared/components/chip-list/chip-list.component.scss'
  // Currently supported: red, green, blue, sand
  getChipClass(tag: string) {
    let suffix = '';
    if (tag === 'bike' || tag === 'mtb' || tag === 'touringbicycle') {
      suffix = '-green';
    } else if (tag === 'hike') {
      suffix = '-sand';
    }
    return `app-chip${suffix}`;
  }

  /* Maybe re-use in location Details
   rateUp(tour: Tour): void {
     tour.rating = tour.rating + 1;
     this.update(tour);
   }

   rateDown(tour: Tour): void {
     tour.rating = (tour.rating > 0) ? tour.rating - 1 : 0;
     this.update(tour);
   }

   private update(tour: Tour) {
     this.logger.info(`${tour.id} new rating ${tour.rating}`);
     this.store.updateItem(tour.id, tour).subscribe(updatedItem => tour = updatedItem);
   }
   */

}
