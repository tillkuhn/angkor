import {Component, Input} from '@angular/core';
import {REGEXP_COORDINATES} from '@shared/domain/smart-coordinates';
import {UntypedFormControl} from '@angular/forms';
import {NGXLogger} from 'ngx-logger';
import {NotificationService} from '@shared/services/notification.service';

@Component({
  selector: 'app-coordinates-input',
  templateUrl: './coordinates-input.component.html',
  styleUrls: ['./coordinates-input.component.scss']
})
export class CoordinatesInputComponent  {

  @Input() formControlInput: UntypedFormControl;
  @Input() formFieldClass = 'app-full-width';

  constructor(private logger: NGXLogger,
              private notifications: NotificationService) {
  }

  // Triggered by button in coordinates input field
  checkCoordinates(event: any) {
    const geoStr = this.formControlInput.value; // formData.value.coordinatesStr;
    if (geoStr) {
      try {
        const newVal = this.parseCoordinates(geoStr);
        // this.formData.patchValue({coordinatesStr: newval});
        this.formControlInput.setValue(newVal);
        this.logger.debug(`${geoStr} parsed to coordinates ${newVal}`);
      } catch (e) {
        this.notifications.warn(e.message);
      }
    }
  }

  parseCoordinates(mapsurl: string): string {
    const match = mapsurl.match(REGEXP_COORDINATES); // match[1]=lat, match[2]=lon or match==null
    if (match == null) {
      throw Error(`${mapsurl} does not match ${REGEXP_COORDINATES}`);
    }
    return `${match[1]},${match[2]}`;
  }

}
