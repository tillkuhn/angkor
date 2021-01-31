// Fix karma tests:
// https://www.hhutzler.de/blog/angular-6-using-karma-testing/#Error_Datails_NullInjectorError_No_provider_for_Router

import {Component, OnInit} from '@angular/core';
import {AuthService} from '../shared/auth.service';
import {NGXLogger} from 'ngx-logger';
import {MasterDataService} from '../shared/master-data.service';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {EnvironmentService} from '../shared/environment.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  account: any;
  greeting = 'Welcome home, TiMaFe guest!';

  constructor(public authService: AuthService,
              private logger: NGXLogger,
              public masterData: MasterDataService,
              private matIconRegistry: MatIconRegistry,
              public envService: EnvironmentService,
              private domSanitizer: DomSanitizer) {
  }

  ngOnInit(): void {
    // https://www.digitalocean.com/community/tutorials/angular-custom-svg-icons-angular-material
    this.matIconRegistry.addSvgIcon(
      `backpack`, this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/backpack.svg')
    );
    this.matIconRegistry.addSvgIcon(
      `noodlebowl`, this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/noodlebowl.svg')
    );
  }

  login() {
    this.logger.info('logint');
    this.authService.login();
  }

  logout() {
    // this.loginService.login();
    this.logger.warn('logout to be implemented');
  }


}
