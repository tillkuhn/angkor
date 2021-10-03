import { Component, OnInit } from '@angular/core';
import {TourStoreService} from '@app/tours/tour-store.service';
import {Tour} from '@domain/location';

@Component({
  selector: 'app-tours',
  templateUrl: './tours.component.html',
  styleUrls: ['./tours.component.scss']
})
export class ToursComponent implements OnInit {

  items: Tour[] = [];

  constructor(public store: TourStoreService
  ) {
  }
  ngOnInit(): void {
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
