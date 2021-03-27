import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Link} from '@app/domain/link';
import {NGXLogger} from 'ngx-logger';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '@shared/services/auth.service';
import {SmartCoordinates} from '@shared/domain/smart-coordinates';

@Component({
  selector: 'app-link-details',
  templateUrl: './link-details.component.html',
  styleUrls: ['./link-details.component.scss']
})
export class LinkDetailsComponent implements OnInit{

  private readonly className = 'LinkDetailsComponent';
  matcher = new DefaultErrorStateMatcher();
  formData: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<LinkDetailsComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Link,
    private formBuilder: FormBuilder,
    public authService: AuthService, // used in form to check if edit is allowed
    private logger: NGXLogger) {
  }

  ngOnInit(): void {
    this.formData = this.formBuilder.group({
      id: [this.data.id],
      linkUrl: [this.data.linkUrl, Validators.required],
      name: [this.data.name, Validators.required],
      mediaType: [this.data.mediaType],
      // todo support array natively
      coordinatesStr: (Array.isArray((this.data.coordinates)) && (this.data.coordinates.length > 1)) ? `${this.data.coordinates[1]},${this.data.coordinates[0]}` : null
    });
    this.logger.debug(`${this.className}.initForm: Finished`);
  }

  closeItem(): void {
    this.dialogRef.close(); // no arg will be considered as cancel
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
