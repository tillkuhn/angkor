import {Component, OnInit} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {Metric} from './metric';
import {EnvironmentService} from '@shared/services/environment.service';
import {AdminService} from '@app/admin/admin.service';

@Component({
  selector: 'app-metrics',
  templateUrl: './metrics.component.html',
  styleUrls: ['../../shared/components/common.component.scss']
})
export class MetricsComponent implements OnInit {

  private readonly className = `MetricsComponent`;

  data: Metric[] = [];
  displayedColumns: string[] = ['name', 'value', 'description'];

  constructor(private api: AdminService,
              private logger: NGXLogger,
              private envService: EnvironmentService
  ) {
  }

  ngOnInit(): void {
    this.api.getMetrics().subscribe(data => {
      this.data = data;
      data.push({name: 'Angular Version', value: this.envService.angularVersion});
      data.push({name: 'App Version (UI)', value: this.envService.appVersion});
    });
  }

  triggerAction(action: string) {
    this.logger.info(`${this.className} trigger action ${action}`);
    this.api.triggerAction(action).subscribe( result => this.logger.info(result));
  }

}
