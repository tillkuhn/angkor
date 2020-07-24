// Fix karma tests:
// https://www.hhutzler.de/blog/angular-6-using-karma-testing/#Error_Datails_NullInjectorError_No_provider_for_Router

import { Component, OnInit } from '@angular/core';
import {AuthService} from '../shared/auth.service';
import {NGXLogger} from 'ngx-logger';
import {getTableUnknownDataSourceError} from '@angular/cdk/table/table-errors';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.sass']
})
export class HomeComponent implements OnInit {

  constructor(private authService: AuthService, private logger: NGXLogger) { }
  account: any;
  authenticated = false;
  greeting = 'Welcome home, TiMaFe guest!';

  ngOnInit(): void {
    this.authService.isAuthenticated.subscribe( isAuthenticated => {
      if (isAuthenticated) {
        this.authenticated = true;
        this.greeting = 'Welcome home, authenticated user!';
      };
    });
  }

  login() {
    this.logger.info('logint');
    this.authService.login();
  }

  logout() {
    // this.loginService.login();
    this.logger.warn('logout to be implemented');
  }


  getAccount() {
    this.authService.getAccount()
      .subscribe((res: any) => {
        this.account = res;
        this.logger.debug('getPlaceAccount', res);
      }, err => {
        this.logger.error('Oha'+err);
      });
  }

}
