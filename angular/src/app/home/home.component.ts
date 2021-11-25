// Fix karma tests:
// https://www.hhutzler.de/blog/angular-6-using-karma-testing/#Error_Datails_NullInjectorError_No_provider_for_Router

import {ActivatedRoute} from '@angular/router';
import {AdminService} from '@app/admin/admin.service';
import {AuthService} from '@shared/services/auth.service';
import {Component, OnInit} from '@angular/core';
import {EnvironmentService} from '@shared/services/environment.service';
import {MasterDataService} from '@shared/services/master-data.service';
import {NGXLogger} from 'ngx-logger';

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
    duration: 1500
  };
  // Entity Counts
  counts = {
    dishes: 0,
    feeds: 0,
    notes: 0,
    places: 0,
    photos: 0,
    pois: 0,
    posts: 0,
    tours: 0,
    videos: 0,
  };
  private readonly className = 'LinkDetailsComponent';

  constructor(public authService: AuthService,
              private api: AdminService, // todo move metrics out of admin service
              private logger: NGXLogger,
              public route: ActivatedRoute,
              public masterData: MasterDataService,
              public env: EnvironmentService,
  ) {
  }

  ngOnInit(): void {
    this.logger.debug(`${this.className}.ngOnInit: Welcome Home`);
    this.api.getStats().subscribe(data => this.counts = data);
  }

}
