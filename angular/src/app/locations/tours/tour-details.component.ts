import {Component, ViewChild} from '@angular/core';
import {TourStoreService} from '@app/locations/tours/tour-store.service';
import {LocationDetailsComponent} from '@app/locations/location-details.component';
import {NGXLogger} from 'ngx-logger';
import {Location, Tour} from '@domain/location';

@Component({
  selector: 'app-tour-details',
  templateUrl: './tour-details.component.html',
  styleUrls: []
})
export class TourDetailsComponent {

private readonly className = 'TourDetailsComponent';

  // https://indepth.dev/posts/1415/implementing-reusable-and-reactive-forms-in-angular-2
  // Note how the @ViewChild options uses the static: true to resolve the component instance as soon as possible
  // so that we have the sub-form instance of the LocationDetailsComponent class.
  @ViewChild(LocationDetailsComponent, { static: true })
  public locationDetails: LocationDetailsComponent;
  item: Tour;

  constructor(
    public store: TourStoreService,
    private logger: NGXLogger,
  ) {}


  // Just to test access to child class
  downloadGPX(): string {
    const externalId = this.locationDetails.formData?.get('externalId')?.value;
    return `https://www.komoot.de/api/v007/tours/${externalId}.gpx`;
  }

  itemLoaded(event: Location) {
    this.logger.info(`${this.className}: Tour ${event.id} loaded`);
    this.item = event as Tour;
  }

}
