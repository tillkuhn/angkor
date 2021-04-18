import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Link} from '@app/domain/link';
import {NGXLogger} from 'ngx-logger';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '@shared/services/auth.service';
import {SmartCoordinates} from '@shared/domain/smart-coordinates';
import {LinkStoreService} from '@app/links/link-store.service';
import {ListItem} from '@shared/domain/list-item';
import {ListType} from '@shared/services/master-data.service';
import {Subject} from 'rxjs';
import {first, takeUntil} from 'rxjs/operators';

@Component({
  selector: 'app-link-details',
  templateUrl: './link-details.component.html',
  styleUrls: ['./link-details.component.scss']
})
export class LinkDetailsComponent implements OnInit {

  private readonly className = 'LinkDetailsComponent';
  mediaTypes: ListItem[] = [];

  matcher = new DefaultErrorStateMatcher();
  formData: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<LinkDetailsComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Link,
    private formBuilder: FormBuilder,
    private logger: NGXLogger,
    public authService: AuthService, // used in form to check if edit is allowed
    private linkService: LinkStoreService
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
        err => this.logger.error(err),
        () => this.logger.debug('Complete'));

    this.logger.debug(`${this.className}.ngOnInit: Finished`);
  }

  closeItem(): void {
    this.dialogRef.close(); // no arg will be considered as cancel
  }

  // todo make component
  getSelectedMediaType(): ListItem {
    return this.mediaTypes.find(mt => mt.value === this.data.mediaType);
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
