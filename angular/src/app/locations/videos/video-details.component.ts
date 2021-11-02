import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Link} from '@app/domain/link';
import {NGXLogger} from 'ngx-logger';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '@shared/services/auth.service';
import {SmartCoordinates} from '@shared/domain/smart-coordinates';
import {LinkStoreService} from '@app/links/link-store.service';
import {ListItem} from '@shared/domain/list-item';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-link-details',
  templateUrl: './link-details.component.html',
  styleUrls: ['./link-details.component.scss']
})
export class LinkDetailsComponent implements OnInit {

  mediaTypes: ListItem[] = [];
  matcher = new DefaultErrorStateMatcher();
  formData: FormGroup;

  private readonly className = 'LinkDetailsComponent';

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: Link,
    public authService: AuthService, // used in form to check if edit is allowed
    public dialogRef: MatDialogRef<LinkDetailsComponent>,
    private formBuilder: FormBuilder,
    private linkService: LinkStoreService,
    private logger: NGXLogger
  ) {
  }

  ngOnInit(): void {
    this.formData = this.formBuilder.group({
      id: [this.data.id],
      linkUrl: [this.data.linkUrl, Validators.required],
      name: [this.data.name, Validators.required],
      mediaType: [this.data.mediaType, Validators.required],
      // todo support array natively
      coordinatesStr: (Array.isArray((this.data.coordinates)) && (this.data.coordinates.length > 1)) ? `${this.data.coordinates[1]},${this.data.coordinates[0]}` : null
    });
    this.linkService.getLinkMediaTypes$()
      // no need to unsubscribe https://stackoverflow.com/a/53347696/4292075
      .pipe(first())
      .subscribe(items => this.mediaTypes = items,
        err => this.logger.error(err));

    this.logger.debug(`${this.className}.ngOnInit: Finished`);
  }

  closeItem(): void {
    this.dialogRef.close(); // no arg will be considered as cancel
  }

  getSelectedMediaType(): ListItem {
    return this.mediaTypes.find(mt => mt.value === this.formData.get('mediaType').value);
  }

  importTour() {
    const tourUrl = this.formData.get('linkUrl').value;
    const match = tourUrl.match(/tour\/(\d+)/); // match[1]=lat, match[2]=lon or match==null
    if (match == null) {
      window.alert(`${tourUrl} does not match expected .../tours/id pattern`);
      return;
    }
    const externalId = match[1];
    this.logger.info(`Importing from ${tourUrl} ${match[1]}`);
    this.linkService.getExternalTour$(externalId).subscribe(tour => {
      this.logger.info(tour);
      this.formData.get('name').patchValue(tour.name);
      this.formData.get('coordinatesStr').patchValue(`${tour.coordinates[1]},${tour.coordinates[0]}`);
    });
  }

  saveItem() {
    // let's do this better soon, also in place edit
    const item = this.formData.value;
    // Todo: validate update coordinates array after they've been entered, not shortly before submit
    if (item.coordinatesStr) {
      const sco = new SmartCoordinates((item.coordinatesStr));
      item.coordinates = sco.lonLatArray;
      this.logger.debug('coordinates', sco);
      delete item.coordinatesStr;
    }
    this.logger.debug(`${this.className}.save`);
    this.dialogRef.close(item as Link);
  }


}
