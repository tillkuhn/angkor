import {ActivatedRoute} from '@angular/router';
import {AuthService} from '@shared/services/auth.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {NGXLogger} from 'ngx-logger';
import {Subject} from 'rxjs';
import {TourDetailsComponent} from '@app/locations/tours/tour-details.component';
import {Location} from '@domain/location';
import {debounceTime, distinctUntilChanged, filter, switchMap, takeUntil} from 'rxjs/operators';
import {EntityTypeInfo, EntityMetadata, EntityType} from '@shared/domain/entities';
import {LocationStoreService} from '@app/locations/location-store.service';
import {VideoDetailsComponent} from '@app/locations/videos/video-details.component';
import {ComponentType} from '@angular/cdk/portal';
import {WithDestroy} from '@shared/mixins/with-destroy';
import {MasterDataService} from '@shared/services/master-data.service';
import {PostDetailsComponent} from '@app/locations/posts/post-details.component';

@Component({
  selector: 'app-location-list',
  templateUrl: './locations.component.html',
  styleUrls: ['./locations.component.scss']
})
export class LocationsComponent extends WithDestroy() implements OnDestroy, OnInit {

  private readonly className = 'LocationsComponent';

  readonly entityTypes: Array<EntityTypeInfo> = [
    EntityMetadata[EntityType.TOUR], EntityMetadata[EntityType.VIDEO], EntityMetadata[EntityType.POST]
  ];

  entityType: EntityType; // set by ngInit based on route data
  items: Location[] = [];
  keyUp$ = new Subject<string>();
  minSearchTermLength = 1; // min number of keyed in chars to trigger a search

  constructor(
    public authService: AuthService,
    public masterData: MasterDataService,
    public store: LocationStoreService,
    private dialog: MatDialog,
    private logger: NGXLogger,
    private route: ActivatedRoute,
  ) {
    // super(store, logger);
    super();
  }

  ngOnInit(): void {
    // Get router data, only works for components that don't navigate: https://stackoverflow.com/a/46697826/4292075
    this.entityType = this.route.snapshot.data.entityType;
    this.logger.info(`${this.className}.ngOnInit(): Warming up for entityType=${this.entityType}`);

    this.store.searchRequest.primarySortProperty = 'updatedAt';
    this.store.searchRequest.sortDirection = 'DESC';
    this.store.searchRequest.entityTypes = [this.entityType];

    this.keyUp$.pipe(
      filter(term => term.length >= this.minSearchTermLength),
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(() => this.store.searchItems()),
      takeUntil(this.destroy$), // avoid leak https://stackoverflow.com/a/41177163/4292075 (take this.destroy$ from mixin)
    ).subscribe(items => this.items = items,
      () => {
      },
      () => this.logger.info(`${this.className}.ngOnInit(): Search completed`)
    );

    // run initial search on page load, comment out if you want to have the search triggered by user interaction
    this.runSearch();
  }

  runSearch() {
    this.store.searchItems().subscribe(items => this.items = items);
  }

  // onMapboxStyleChange is triggered when the user selects a different style, e.g. switches to street view
  onEntityTypesChange(entry: { [key: string]: any }) {
    this.logger.info(`${this.className} Switch to entityType Filter ${entry.id}`);
    this.store.searchRequest.entityTypes = [entry.id]; // todo handle multiple
    this.runSearch();
  }

  previewImageUrl(item: Location) {
    if (!item.imageUrl) {
      return EntityMetadata[item.entityType].iconUrl;
    // See videos/README.adoc replace high res image with small (default.jpg) 120px image to save bandwidth
    } else if (item.imageUrl.toLowerCase().startsWith('https://img.youtube.com/')) {
      return item.imageUrl.replace('/sddefault.jpg', '/default.jpg');
    } else {
      return item.imageUrl;
    }
  }

  routerLink(item: Location) {
    let path: string;
    switch (item.entityType) {
      case EntityType.VIDEO:
        path = `/player/${item.id}`;
        break;
      default:
        this.logger.trace(`${item.entityType} router link not yet supported`);
    }
    return path;
  }

  // Open Details, delegate to entity specific component
  openDetailsDialog(item: Location): void {
    let componentClass: ComponentType<unknown>;
    switch (item.entityType) {
      case EntityType.VIDEO:
        componentClass = VideoDetailsComponent;
        break;
      case EntityType.TOUR:
        componentClass = TourDetailsComponent;
        break;
      case EntityType.POST:
        componentClass = PostDetailsComponent;
        break;
      default:
        throw new Error(`${item.entityType} not yet supported`);
    }

    const dialogRef = this.dialog.open(componentClass, {
      // width: '75%', maxWidth: '600px',
      // dims etc. now defined centrally in styles.scss (with .mat-dialog-container)
      panelClass: 'app-details-panel',
      data: {id: item.id}
    });

    dialogRef.afterClosed().subscribe(result => {
      this.logger.debug(`${this.className}.dialogRef.afterClosed: ${result}`);
    });
  }

  /*
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

  getChipClass(tag: string) {
    let suffix = '';
    if (tag === 'bike' || tag === 'mtb') {
      suffix = '-green';
    }
    return `app-chip${suffix}`;
  }


  /* // Handled by mixin
  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
  */

}
