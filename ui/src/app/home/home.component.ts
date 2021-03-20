// Fix karma tests:
// https://www.hhutzler.de/blog/angular-6-using-karma-testing/#Error_Datails_NullInjectorError_No_provider_for_Router

import {Component, OnInit} from '@angular/core';
import {AuthService} from '../shared/services/auth.service';
import {NGXLogger} from 'ngx-logger';
import {MasterDataService} from '../shared/services/master-data.service';
import {EnvironmentService} from '../shared/services/environment.service';
import {ApiService} from '../shared/services/api.service';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss', '../shared/components/common.component.scss']
})
export class HomeComponent implements OnInit {

  countUpConfig = {
    width: '32px',
    height: '32px',
    borderRadius: '60px',
    fontSize: '24px',
    padding: '18px',
    duration: 2000
  };

  // Entity Counts
  placesCount = 0;
  dishesCount = 0;
  poisCount = 0;
  notesCount = 0;
  videosCount = 0;

  constructor(public authService: AuthService,
              private api: ApiService,
              private logger: NGXLogger,
              public route: ActivatedRoute,
              public masterData: MasterDataService,
              public env: EnvironmentService,
  ) {
  }

  ngOnInit(): void {
    this.logger.debug('Welcome Home');
    this.api.getStats().subscribe(data => {
      this.placesCount = data.places;
      this.dishesCount = data.dishes;
      this.poisCount = data.pois;
      this.notesCount = data.notes;
    });
  }


}
