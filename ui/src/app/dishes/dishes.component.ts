import {Component, OnInit} from '@angular/core';
import {ApiService} from '../shared/api.service';
import {EnvironmentService} from '../shared/environment.service';
import {NGXLogger} from 'ngx-logger';
import {Dish} from '../domain/dish';
import {MasterDataService} from '../shared/master-data.service';

@Component({
  selector: 'app-dishes',
  templateUrl: './dishes.component.html',
  styleUrls: ['./dishes.component.scss']
})
export class DishesComponent implements OnInit {

  displayedColumns: string[] = ['areaCode', 'name', 'authScope'];
  data: Dish[] = [];

  constructor(private api: ApiService,
              private env: EnvironmentService,
              private masterData: MasterDataService,
              private logger: NGXLogger) {
  }

  ngOnInit() {
    this.api.getDishes()
      .subscribe((res: any) => {
        this.data = res;
        this.logger.debug('getDishes()', this.data);
      }, err => {
        this.logger.error(err);
      });
  }

}
