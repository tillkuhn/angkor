import {Component, OnInit} from '@angular/core';
import {ApiService} from '../../shared/api.service';
import {NGXLogger} from 'ngx-logger';
import {Metric} from './metric';
import {EnvironmentService} from '../../shared/environment.service';

@Component({
  selector: 'app-metrics',
  templateUrl: './metrics.component.html',
})
export class MetricsComponent implements OnInit {
  data: Metric[] = [];
  displayedColumns: string[] = ['name', 'value', 'description'];

  constructor(private api: ApiService, private logger: NGXLogger,
              private envService: EnvironmentService
  ) {}

  ngOnInit(): void {
    this.api.getMetrics().subscribe(data => {
      this.data = data;
      data.push({name: 'Angular Version', value: this.envService.angularVersion});
      data.push({name: 'App Version (UI)', value: this.envService.appVersion});
    });
  }

}
