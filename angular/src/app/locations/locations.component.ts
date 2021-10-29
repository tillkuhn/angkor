import {ActivatedRoute} from '@angular/router';
import {AuthService} from '@shared/services/auth.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {NGXLogger} from 'ngx-logger';
import {Subject} from 'rxjs';
import {TourDetailsComponent} from '@app/locations/tours/tour-details.component';
import {Location} from '@domain/location';
import {debounceTime, distinctUntilChanged, filter, switchMap, takeUntil} from 'rxjs/operators';
import {EntityType} from '@shared/domain/entities';
import {LocationStoreService} from '@app/locations/location-store.service';

@Component({
  selector: 'app-location-list',
  templateUrl: './locations.component.html',
  styleUrls: ['./locations.component.scss']
})
export class LocationsComponent implements OnDestroy, OnInit {

  private readonly className = 'LocationsComponent';
  private ngUnsubscribe = new Subject();

  readonly entityTypes = [
    {
      description: 'Tours',
      icon: 'tour',
      id: EntityType.TOUR
    },
    {
      description: 'Videos',
      icon: 'video',
      id: EntityType.VIDEO
    }
  ];

  items: Location[] = [];
  keyUp$ = new Subject<string>();
  minSearchTermLength = 1; // min number of keyed in chars to trigger a search
  entityType: EntityType;

  constructor(
    public authService: AuthService,
    public store: LocationStoreService,

    private dialog: MatDialog,
    private logger: NGXLogger,
    private route: ActivatedRoute,
  ) {
  }

  ngOnInit(): void {
    // Get router data, only works for components that don't navigate: https://stackoverflow.com/a/46697826/4292075
    this.entityType = this.route.snapshot.data.entityType;
    this.logger.info(`${this.className}.ngOnInit(): Warming up for entityType=${this.entityType}`);

    this.store.searchRequest.primarySortProperty = 'updatedAt';
    this.store.searchRequest.sortDirection = 'DESC';
    this.store.searchRequest.entityTypes = [this.entityTypes[0].id];

    this.keyUp$.pipe(
      filter(term => term.length >= this.minSearchTermLength),
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(() => this.store.searchItems()),
      takeUntil(this.ngUnsubscribe), // avoid leak https://stackoverflow.com/a/41177163/4292075
    ).subscribe(items => this.items = items,
      () => {},
      () => this.logger.info(`${this.className}.ngOnInit(): Search completed`)
    );

    // run initial search on page load, uncomment if you want to run a search on initial page load
    this.runSearch();
  }

  runSearch() {
    this.store.searchItems().subscribe(items => this.items = items);
  }


  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  // onMapboxStyleChange is triggered when the user selects a different style, e.g. switches to street view
  onEntityTypesChange(entry: { [key: string]: any }) {
    this.logger.info(`${this.className} Switch to entityType Filter ${entry.id}`);
    this.store.searchRequest.entityTypes = [ entry.id ]; // todo handle multiple
    this.runSearch();
  }

  // Input new Video
  openDetailsDialog(data: Location): void {
    const dialogRef = this.dialog.open(TourDetailsComponent, {
      // width: '75%', maxWidth: '600px',
      // dims etc. now defined centrally in styles.scss (with .mat-dialog-container)
      panelClass: 'app-details-panel',
      data
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

}
