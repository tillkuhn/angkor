import { Component, OnInit } from '@angular/core';
import {LoginService} from '../core/login.service';
import {NGXLogger} from 'ngx-logger';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.sass']
})
export class HomeComponent implements OnInit {

  constructor(private loginService: LoginService,  private logger: NGXLogger) { }

  ngOnInit(): void {
  }

  login() {
    this.logger.info('logint');
    this.loginService.login();
  }

  logout() {
    // this.loginService.login();
    this.logger.info('logout');
  }
}
