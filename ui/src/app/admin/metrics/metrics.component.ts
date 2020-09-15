import {Component, OnInit} from '@angular/core';
import {ApiService} from '../../shared/api.service';
import {NGXLogger} from 'ngx-logger';
import {FormBuilder} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Note} from '../../domain/note';
import {Metric} from './metric';
import {EnvironmentService} from '../../environment.service';
import {environment} from '../../../environments/environment';
@Component({
  selector: 'app-metrics',
  templateUrl: './metrics.component.html',
})
export class MetricsComponent implements OnInit {
  config: any;
  data: Metric[] = [];
  displayedColumns: string[] = ['name', 'value', 'description'];

  constructor(private api: ApiService, private logger: NGXLogger,
              private envService: EnvironmentService
  ) {
  }

  ngOnInit(): void {
    this.config = {
      angularVersion: this.envService.angularVersion,
      apiUrlRoot: environment.apiUrlRoot
    };
    this.api.getMetrics().subscribe(data => this.data = data);
  }

}
