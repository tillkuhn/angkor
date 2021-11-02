import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {NGXLogger} from 'ngx-logger';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '@shared/services/auth.service';
import {SmartCoordinates} from '@shared/domain/smart-coordinates';
import {VideoStoreService} from '@app/locations/videos/video-store.service';
import {ManagedEntity} from '@shared/domain/entities';
import {Video} from '@domain/location';

@Component({
  selector: 'app-video-details',
  templateUrl: './video-details.component.html',
  styleUrls: [] // './video-details.component.scss'
})
export class VideoDetailsComponent implements OnInit {

  // mediaTypes: ListItem[] = [];
  matcher = new DefaultErrorStateMatcher();
  formData: FormGroup;

  private readonly className = 'VideoDetailsComponent';

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: ManagedEntity, // Todo Dialog data specific object
    public authService: AuthService, // used in form to check if edit is allowed
    public dialogRef: MatDialogRef<VideoDetailsComponent>,
    private formBuilder: FormBuilder,
    private store: VideoStoreService,
    private logger: NGXLogger
  ) {
  }

  ngOnInit(): void {
    this.loadItem(this.data.id); // take from MAT_DIALOG_DATA
    this.formData = this.formBuilder.group({
      authScope: [null, Validators.required],
      coordinatesStr: [null],       // todo support array natively
      imageUrl: [null],
      name: [null, Validators.required],
      primaryUrl: [null, Validators.required],
      tags: this.formBuilder.array([])
    });
    this.logger.debug(`${this.className}.ngOnInit: Finished`);
  }

  loadItem(id: string) {
    this.store.getItem(id).subscribe((item: Video) => {
      // this.id = data.id;
      // use patch on the reactive form data, not set. See
      // https://stackoverflow.com/questions/51047540/angular-reactive-form-error-must-supply-a-value-for-form-control-with-name
      this.formData.patchValue({
        authScope: item.authScope,
        coordinatesStr: (Array.isArray((item.coordinates)) && (item.coordinates.length > 1)) ? `${item.coordinates[1]},${item.coordinates[0]}` : null,
        imageUrl: item.imageUrl,
        name: item.name,
        primaryUrl: item.primaryUrl,
      });
      // patch didn't work if the form is an array, this workaround does. See
      // https://www.cnblogs.com/Answer1215/p/7376987.html [Angular] Update FormArray with patchValue
      if (item.tags) {
        for (const tagItem of item.tags) {
          (this.formData.get('tags') as FormArray).push(new FormControl(tagItem));
        }
      }
    });
  }

  saveItem() {
    const item = this.formData.value;
    // Todo: validate update coordinates array after they've been entered, not shortly before submit
    if (item.coordinatesStr) {
      const sco = new SmartCoordinates((item.coordinatesStr));
      item.coordinates = sco.lonLatArray;
      this.logger.debug('coordinates', sco);
      delete item.coordinatesStr;
    }
    this.logger.debug(`${this.className}.saveItem:`, item);
    this.store.updateItem(this.data.id, this.formData.value)
      .subscribe((res: any) => {
          // this.navigateToItemDetails(res.id);
          this.dialogRef.close(res);
        }, (err: any) => {
          this.logger.error(err);
        }
      );
  }

  closeItem(): void {
    this.dialogRef.close(); // no arg will be considered as cancel
  }


  // getSelectedMediaType(): ListItem {
  //   return this.mediaTypes.find(mt => mt.value === this.formData.get('mediaType').value);
  // }

  // importTour() {
  //   const tourUrl = this.formData.get('linkUrl').value;
  //   const match = tourUrl.match(/tour\/(\d+)/); // match[1]=lat, match[2]=lon or match==null
  //   if (match == null) {
  //     window.alert(`${tourUrl} does not match expected .../tours/id pattern`);
  //     return;
  //   }
  //   const externalId = match[1];
  //   this.logger.info(`Importing from ${tourUrl} ${match[1]}`);
  //   this.linkService.getExternalTour$(externalId).subscribe(tour => {
  //     this.logger.info(tour);
  //     this.formData.get('name').patchValue(tour.name);
  //     this.formData.get('coordinatesStr').patchValue(`${tour.coordinates[1]},${tour.coordinates[0]}`);
  //   });
  // }

}
