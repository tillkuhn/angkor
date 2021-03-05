// Fix karma tests:
// https://www.hhutzler.de/blog/angular-6-using-karma-testing/#Error_Datails_NullInjectorError_No_provider_for_Router

import {Component, OnInit} from '@angular/core';
import {AuthService} from '../shared/services/auth.service';
import {NGXLogger} from 'ngx-logger';
import {MasterDataService} from '../shared/services/master-data.service';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {EnvironmentService} from '../shared/services/environment.service';
import {ActivatedRoute} from '@angular/router';
import {ApiService} from '../shared/services/api.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
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
  placesCount = 0;
  dishesCount = 0;
  poisCount = 0;
  notesCount = 0;


  constructor(public authService: AuthService,
              private api: ApiService,
              private logger: NGXLogger,
              private route: ActivatedRoute,
              public masterData: MasterDataService,
              private matIconRegistry: MatIconRegistry,
              public env: EnvironmentService,
              private domSanitizer: DomSanitizer) {
  }

  ngOnInit(): void {
    // https://www.digitalocean.com/community/tutorials/angular-custom-svg-icons-angular-material
    this.trustIcon('backpack', '../assets/backpack.svg');
    this.trustIcon('noodlebowl', '../assets/noodlebowl.svg');
    this.trustIcon('chilis',   '../assets/chilis.svg');
    this.trustIcon('notebook',   '../assets/notebook.svg');
    this.trustIcon('world',   '../assets/world.svg');
    this.api.getStats().subscribe(data => {
      this.placesCount = data.places;
      this.dishesCount = data.dishes;
      this.poisCount = data.pois;
      this.notesCount = data.notes;
    });
  }

  private trustIcon(iconName: string, resourceUrl: string): void {
    this.matIconRegistry.addSvgIcon(
      iconName, this.domSanitizer.bypassSecurityTrustResourceUrl(resourceUrl)
    );
  }

}
