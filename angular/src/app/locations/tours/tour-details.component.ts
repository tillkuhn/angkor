import {Component} from '@angular/core';
import {TourStoreService} from '@app/locations/tours/tour-store.service';

@Component({
  selector: 'app-tour-details',
  templateUrl: './tour-details.component.html',
  styleUrls: []
})
export class TourDetailsComponent {

  constructor(
    public store: TourStoreService,
  ) {}

}
