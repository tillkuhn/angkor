import {Component, OnInit} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {Metric} from './metric';
import {EnvironmentService} from '@shared/services/environment.service';
import {AdminService} from '@app/admin/admin.service';
import {NotificationService} from '@shared/services/notification.service';

@Component({
  selector: 'app-metrics',
  templateUrl: './metrics.component.html',
  styleUrls: ['../../shared/components/common.component.scss']
})
export class MetricsComponent implements OnInit {

  private readonly className = `MetricsComponent`;

  data: Metric[] = [];
  actions: Map<string, string> = new Map<string, string>();

  displayedColumns: string[] = ['name', 'value', 'description'];

  constructor(private api: AdminService,
              private logger: NGXLogger,
              private envService: EnvironmentService,
              private notificationService: NotificationService,
  ) {
  }

  ngOnInit(): void {
    this.api.getMetrics().subscribe(data => {
      this.data = data;
      data.push({name: 'Angular Version', value: this.envService.angularVersion});
      data.push({name: 'App Version (UI)', value: this.envService.appVersion});
    });
    this.api.actions().subscribe( data => {
      // this.logger.info(JSON.stringify(data));
      this.actions = data;
    });
  }

  triggerAction(action: string) {
    this.logger.info(`${this.className} trigger action ${action}`);
    this.api.triggerAction(action).subscribe(
      result => this.notificationService.success(JSON.stringify(result)));
  }

}
