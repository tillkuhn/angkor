import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ApiService} from '../../shared/api.service';
import {MasterDataService} from '../../shared/master-data.service';
import {NGXLogger} from 'ngx-logger';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '../../shared/auth.service';
import {SmartCoordinates} from '../../domain/smart-coordinates';
import {Dish} from '../../domain/dish';

@Component({
  selector: 'app-dish-detail',
  templateUrl: './dish-detail.component.html',
  styleUrls: ['./dish-detail.component.scss']
})
export class DishDetailComponent implements OnInit {

  item: Dish;

  constructor(private route: ActivatedRoute, private api: ApiService,
              public masterData: MasterDataService, public authService: AuthService,
              private router: Router, private logger: NGXLogger) {
  }

  ngOnInit() {
    this.getItem(this.route.snapshot.params.id);
  }

  getItem(id: any) {
    this.api.getDish(id)
      .subscribe((data: any) => {
        this.item = data;
        this.logger.debug('getItem()', this.item);
      });
  }

  justServed() {
    this.api.justServed(this.item.id)
      .subscribe((data: any) => {
        this.item.timesServed = data.result;
        this.logger.debug('justServed()', data.result);
      });
  }

}
