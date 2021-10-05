import {Component, OnInit} from '@angular/core';
import {TourStoreService} from '@app/tours/tour-store.service';
import {Tour} from '@domain/location';
import {debounceTime, distinctUntilChanged, filter, switchMap} from 'rxjs/operators';
import {Subject} from 'rxjs';

@Component({
  selector: 'app-tours',
  templateUrl: './tours.component.html',
  styleUrls: ['./tours.component.scss']
})
export class ToursComponent implements OnInit {

  items: Tour[] = [];
  keyUp$ = new Subject<string>();
  minSearchTermLength = 1;

  constructor(public store: TourStoreService
  ) {
  }

  ngOnInit(): void {
    this.store.searchRequest.primarySortProperty = "beenThere"
    this.store.searchRequest.sortDirection = 'DESC'
    this.keyUp$.pipe(
      filter(term => term.length >= this.minSearchTermLength),
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(() => this.store.searchItems()), // could use searchTerm as function param param but
    ).subscribe(items => this.items = items);
    // this.runSearch(); // uncomment if you want to run a search on initial page load
  }

  runSearch() {
    this.store.searchItems().subscribe(items => this.items = items);
  }

  getChipClass(tag: string) {
    let suffix = '';
    if (tag === 'bike' || tag === 'mtb') {
      suffix = '-green';
    }
    return `app-chip${suffix}`;
  }

}
