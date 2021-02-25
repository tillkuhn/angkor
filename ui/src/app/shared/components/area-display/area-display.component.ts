import {Component, Input, OnInit} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {MasterDataService} from '../../services/master-data.service';
import {ApiService} from '../../services/api.service';
import {Area} from '../../../domain/area';

export declare type AreaDisplaySize = 'small' | 'medium' | 'big';

/**
 * Usage: <app-area-display [areaCode]="row.areaCode" size="big"></app-area-display>
 */
@Component({
  selector: 'app-area-display',
  templateUrl: './area-display.component.html',
  styleUrls: ['./area-display.component.scss']
})
export class AreaDisplayComponent implements OnInit {

  areas: Area[] = [];
  @Input() areaCode: string;
  @Input() size: AreaDisplaySize = 'medium';
  title = 'Area Flag'; // default

  constructor(private api: ApiService,
              private logger: NGXLogger,
              public masterData: MasterDataService) {
  }

  ngOnInit(): void {
    this.masterData.countries
      .subscribe((res: any) => {
        this.areas = res;
        for (const area of this.areas) {
          if (area.code === this.areaCode) {
            this.title = `${area.name} (${this.areaCode})`;
            break;
          }
        }
      }, err => {
        this.logger.error(err);
      });
  }

  flagCode(areaCode: string): string {
    return areaCode?.includes('-') ? areaCode.split('-')[0] : areaCode;
  }

}
