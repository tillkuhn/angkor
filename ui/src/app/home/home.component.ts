// Fix karma tests:
// https://www.hhutzler.de/blog/angular-6-using-karma-testing/#Error_Datails_NullInjectorError_No_provider_for_Router

import { Component, OnInit } from '@angular/core';
import {AuthService} from '../shared/auth.service';
import {NGXLogger} from 'ngx-logger';
import {getTableUnknownDataSourceError} from '@angular/cdk/table/table-errors';
import {MasterDataService} from '../shared/master-data.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.sass']
})
export class HomeComponent implements OnInit {

  constructor(public authService: AuthService, private logger: NGXLogger, public masterData: MasterDataService) { }
  account: any;
  greeting = 'Welcome home, TiMaFe guest!';

  ngOnInit(): void {}

  login() {
    this.logger.info('logint');
    this.authService.login();
  }

  logout() {
    // this.loginService.login();
    this.logger.warn('logout to be implemented');
  }



}
