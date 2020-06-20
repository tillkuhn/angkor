import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Place } from '../place';
import { Environment } from '../environment';

@Component({
  selector: 'app-places',
  templateUrl: './places.component.html',
  styleUrls: ['./places.component.scss']
})
export class PlacesComponent implements OnInit {

  displayedColumns: string[] = ['name','country', 'summary'];
  data: Place[] = [];
  isLoadingResults = true;

  constructor(private api: ApiService, private env: Environment ) { }

  ngOnInit() {
    this.api.getPlaces()
      .subscribe((res: any) => {
        this.data = res;
        console.log('getPlaces()',this.env.version, this.data);
        this.isLoadingResults = false;
      }, err => {
        console.log(err);
        this.isLoadingResults = false;
      });
  }

}
