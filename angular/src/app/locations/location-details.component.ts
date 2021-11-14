import {Component, EventEmitter, Inject, Input, OnInit, Output} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {Location} from '@domain/location';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {SmartCoordinates} from '@shared/domain/smart-coordinates';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {EntityStore} from '@shared/services/entity-store';
import {AuthService} from '@shared/services/auth.service';
import {EntityDialogRequest, EntityDialogResponse, EntityDialogResult} from '@app/locations/entity-dialog';

/**
 * Should be extended by Entity specific component classes
 *
 * See https://indepth.dev/posts/1415/implementing-reusable-and-reactive-forms-in-angular-2
 * Implementing reusable and reactive forms in Angular
 */
// @Injectable() // needed if abstract, see https://stackoverflow.com/a/64964736/4292075
// skip implements OnInit b/c of lint error
// "Angular will not invoke the `ngOnInit` lifecycle method within `@Injectable()` classes"
@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.component.html',
  styleUrls: []
})
export class LocationDetailsComponent<E extends Location>  implements OnInit {

  protected readonly className = `LocationDetailsComponent`;

  @Input() store: EntityStore<any, any>;
  @Output() itemLoaded = new EventEmitter<E>();

  matcher = new DefaultErrorStateMatcher();
  formData: FormGroup;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: EntityDialogRequest,
    public dialogRef: MatDialogRef<any>, // TODO generic extends LocationDetailsComponent
    public authService: AuthService,
    protected formBuilder: FormBuilder,
    protected logger: NGXLogger
  ) {
  }

  ngOnInit(): void {
    this.logger.info(`${this.className}ngOnInit: dialogData ${JSON.stringify(this.data)}`);
    this.loadItem(this.data.id); // take from MAT_DIALOG_DATA
    this.formData = this.formBuilder.group({
      areaCode: [null],
      authScope: [null, Validators.required],
      coordinatesStr: [null],       // todo support array natively
      externalId: [null],
      geoAddress: [null],
      imageUrl: [null],
      name: [null, Validators.required],
      primaryUrl: [null, Validators.required],
      tags: this.formBuilder.array([]),
    });
    this.logger.debug(`${this.className}.ngOnInit: Finished`);
  }

  loadItem(id: string) {
    this.store.getItem(id).subscribe((item: E) => {
      // this.id = data.id;
      // use patch on the reactive form data, not set. See
      // https://stackoverflow.com/questions/51047540/angular-reactive-form-error-must-supply-a-value-for-form-control-with-name
      this.formData.patchValue({
        areaCode: item.areaCode,
        authScope: item.authScope,
        coordinatesStr: (Array.isArray((item.coordinates)) && (item.coordinates.length > 1)) ? `${item.coordinates[1]},${item.coordinates[0]}` : null,
        externalId: item.externalId,
        geoAddress: item.geoAddress,
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
      this.itemLoaded.emit(item);
    });
  }

  saveItem() {
    const item = this.formData.value;
    // Todo: validate update coordinates array after they've been entered, not shortly before submit
    if (item.coordinatesStr) {
      const sco = new SmartCoordinates((item.coordinatesStr));
      item.coordinates = sco.lonLatArray;
      this.logger.trace('coordinates', sco);
      delete item.coordinatesStr;
    }
    this.logger.debug(`${this.className}.saveItem:`, item);
    this.store.updateItem(this.data.id, this.formData.value)
      .subscribe((res: any) => {
          // this.navigateToItemDetails(res.id);
          this.closeDialog('Updated', res);
        }, (err: any) => {
          this.logger.error(err);
        }
      );
  }

  closeDialog(result: EntityDialogResult, entity?: Location) {
    this.logger.debug(`${this.className}.closeDialog: ${result}`);
    const response: EntityDialogResponse<Location> = {result, entity}; // short for result: result
    this.dialogRef.close(response);
  }


}
