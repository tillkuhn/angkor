import {Component, Input, OnInit} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {MasterDataService} from '@shared/services/master-data.service';
import {ApiService} from '@shared/services/api.service';
import {Area} from '../../../domain/area';

// Freak Flags uses css constructor classes to choose a display size: ff-sm, ff-md, ff-lg, ff-xl.
export declare type AreaDisplaySize = 'sm' | 'md' | 'lg' | 'xl';

// Styles ff-round, ff-app, ff-sphere
export declare type AreaDisplayStyle = 'round' | 'app' | 'sphere';

/**
 * Usage: <app-area-display [areaCode]="row.areaCode" displaySize="xl" displayStyle="sphere"></app-area-display>
 * Thanks to https://www.freakflagsprite.com/
 */
@Component({
  selector: 'app-area-display',
  templateUrl: './area-display.component.html',
  styleUrls: ['./area-display.component.scss']
})
export class AreaDisplayComponent implements OnInit {

  areas: Area[] = [];
  @Input() areaCode: string;
  @Input() displaySize: AreaDisplaySize = 'xl';
  @Input() displayStyle: AreaDisplayStyle = 'sphere';

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
    const code =  areaCode?.includes('-') ? areaCode.split('-')[0] : areaCode;
    return code?.toUpperCase();
  }

}
