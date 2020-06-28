import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../api.service';
import { Place } from '../domain/place';
import {NGXLogger} from 'ngx-logger';

@Component({
  selector: 'app-place-detail',
  templateUrl: './place-detail.component.html',
  styleUrls: ['./place-detail.component.scss']
})
export class PlaceDetailComponent implements OnInit {

  place: Place = { id: '', name: '', country: '', summary: '', imageUrl: ''/*, updated: null*/ };
  isLoadingResults = true;

  constructor(private route: ActivatedRoute, private api: ApiService,
              private router: Router,  private logger: NGXLogger) { }

  ngOnInit() {
    this.getPlaceDetails(this.route.snapshot.params.id);
  }

  getPlaceDetails(id: any) {
    this.api.getPlace(id)
      .subscribe((data: any) => {
        this.place = data;
        this.logger.debug('getPlaceDetails',this.place);
        this.isLoadingResults = false;
      });
  }

  deletePlace(id: any) {
    this.isLoadingResults = true;
    this.api.deletePlace(id)
      .subscribe(res => {
          this.isLoadingResults = false;
          this.router.navigate(['/places']);
        }, (err) => {
          this.logger.error('deletePlace',err);
          this.isLoadingResults = false;
        }
      );
  }

}
