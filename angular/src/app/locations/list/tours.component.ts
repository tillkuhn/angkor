import {ActivatedRoute} from '@angular/router';
import {AuthService} from '@shared/services/auth.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {NGXLogger} from 'ngx-logger';
import {Subject} from 'rxjs';
import {TourDetailsComponent} from '@app/locations/details/tour-details.component';
import {TourStoreService} from '@app/locations/tour-store.service';
import {Tour} from '@domain/location';
import {debounceTime, distinctUntilChanged, filter, switchMap, takeUntil} from 'rxjs/operators';
import {EntityType} from '@shared/domain/entities';

@Component({
  selector: 'app-tours',
  templateUrl: './tours.component.html',
  styleUrls: ['./tours.component.scss']
})
export class ToursComponent implements OnDestroy, OnInit {

  private readonly className = 'ToursComponent';
  private ngUnsubscribe = new Subject();

  items: Tour[] = [];
  keyUp$ = new Subject<string>();
  minSearchTermLength = 1;
  entityType: EntityType;

  constructor(
    public authService: AuthService,
    public store: TourStoreService,

    private dialog: MatDialog,
    private logger: NGXLogger,
    private route: ActivatedRoute,
  ) {
  }

  ngOnInit(): void {
    // Get router data, only works for components that don't navigate: https://stackoverflow.com/a/46697826/4292075
    this.entityType = this.route.snapshot.data.entityType;
    this.logger.info(`${this.className}.ngOnInit(): Warming up for entityType=${this.entityType}`);
    this.store.searchRequest.primarySortProperty = 'beenThere';
    this.store.searchRequest.sortDirection = 'DESC';

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

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  runSearch() {
    this.store.searchItems().subscribe(items => this.items = items);
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

  getChipClass(tag: string) {
    let suffix = '';
    if (tag === 'bike' || tag === 'mtb') {
      suffix = '-green';
    }
    return `app-chip${suffix}`;
  }

}
