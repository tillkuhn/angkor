import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../shared/api.service';
import {NGXLogger} from 'ngx-logger';
import {Place} from '../domain/place';
import {MasterDataService} from '../shared/master-data.service';
import {GeoPoint} from '../domain/geo-point';

@Component({
  selector: 'app-place-detail',
  templateUrl: './place-detail.component.html',
  styleUrls: ['./place-detail.component.scss']
})
export class PlaceDetailComponent implements OnInit {

  place: Place = { id: '', name: '', areaCode: ''};
  coordinates: GeoPoint;

  constructor(private route: ActivatedRoute, private api: ApiService, public masterData: MasterDataService,
              private router: Router,  private logger: NGXLogger) { }

  ngOnInit() {
    this.getPlaceDetails(this.route.snapshot.params.id);
  }

  getPlaceDetails(id: any) {
    this.api.getPlace(id)
      .subscribe((data: any) => {
        this.place = data;
        if (this.place.coordinates && this.place.coordinates.length > 1 ) {
          this.coordinates = new GeoPoint(this.place.coordinates);
        }
        this.logger.debug('getPlaceDetails()', this.place);
      });
  }

  deletePlace(id: any) {
    this.api.deletePlace(id)
      .subscribe(res => {
          this.router.navigate(['/places']);
        }, (err) => {
          this.logger.error('deletePlace', err);
        }
      );
  }

}
