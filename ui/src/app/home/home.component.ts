// Fix karma tests:
// https://www.hhutzler.de/blog/angular-6-using-karma-testing/#Error_Datails_NullInjectorError_No_provider_for_Router

import { Component, OnInit } from '@angular/core';
import {LoginService} from '../shared/login.service';
import {NGXLogger} from 'ngx-logger';
import {ApiService} from '../api.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.sass']
})
export class HomeComponent implements OnInit {

  constructor(private loginService: LoginService, private api: ApiService,  private logger: NGXLogger) { }
  account: any;
  isAuthenticated: boolean;

  ngOnInit(): void {
  }

  login() {
    this.logger.info('logint');
    this.loginService.login();
  }

  logout() {
    // this.loginService.login();
    this.logger.warn('logout to be implemented');
  }


  getAccount() {
    this.api.isAuthenticated().subscribe( (res: boolean) =>  {
      this.isAuthenticated = res;
    })
    this.api.getAccount()
      .subscribe((res: any) => {
        this.account = res;
        this.logger.debug('getPlaceAccount', res);
      }, err => {
        this.logger.error('Oha'+err);
      });
  }

}
