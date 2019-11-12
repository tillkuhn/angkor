import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../api.service';
import { Place } from '../place';

@Component({
  selector: 'app-place-detail',
  templateUrl: './place-detail.component.html',
  styleUrls: ['./place-detail.component.scss']
})
export class PlaceDetailComponent implements OnInit {

  place: Place = { id: '', name: '', desc: ''/*, updated: null*/ };
  isLoadingResults = true;

  constructor(private route: ActivatedRoute, private api: ApiService, private router: Router) { }

  ngOnInit() {
    this.getPlaceDetails(this.route.snapshot.params['id']);
  }

  getPlaceDetails(id: any) {
    this.api.getPlace(id)
      .subscribe((data: any) => {
        this.place = data;
        console.log(this.place);
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
          console.log(err);
          this.isLoadingResults = false;
        }
      );
  }

}
