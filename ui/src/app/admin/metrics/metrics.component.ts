import { Component, OnInit } from '@angular/core';
import {ApiService} from '../../shared/api.service';
import {NGXLogger} from 'ngx-logger';
import {FormBuilder} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Note} from '../../domain/note';
import {Metric} from './metric';

@Component({
  selector: 'app-metrics',
  templateUrl: './metrics.component.html',
})
export class MetricsComponent implements OnInit {
  data: Metric[] = [];
  displayedColumns: string[] = ['name', 'value', 'description'];

  constructor(private api: ApiService, private logger: NGXLogger) {
  }
  ngOnInit(): void {
    this.api.getMetrics().subscribe(data => this.data = data);
  }

}
