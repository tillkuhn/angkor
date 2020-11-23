import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ApiService} from '../../shared/api.service';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {NGXLogger} from 'ngx-logger';
import {Area} from '../../domain/area';
import {MyErrorStateMatcher} from '../../shared/form-helper';
import {ListType, MasterDataService} from '../../shared/master-data.service';
import {ListItem} from '../../domain/list-item';
import {SmartCoordinates} from '../../domain/smart-coordinates';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '../../shared/auth.service';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {MatChipInputEvent} from '@angular/material/chips';
import {FileService} from '../../shared/file.service';
import {Clipboard} from '@angular/cdk/clipboard';

@Component({
  selector: 'app-place-edit',
  templateUrl: './place-edit.component.html',
  styleUrls: ['./place-edit.component.scss']
})
export class PlaceEditComponent implements OnInit {

  countries: Area[] = [];
  locationTypes: ListItem[];
  authScopes: ListItem[];
  formData: FormGroup;
  id = '';
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];   // For Tag support
  matcher = new MyErrorStateMatcher();



  constructor(private router: Router,
              private route: ActivatedRoute,
              private api: ApiService,
              private fileService: FileService,
              private formBuilder: FormBuilder,
              private snackBar: MatSnackBar,
              public authService: AuthService,
              private logger: NGXLogger,
              private clipboard: Clipboard,
              public masterData: MasterDataService) {
  }

  ngOnInit() {
    this.getPlace(this.route.snapshot.params.id);
    this.api.getCountries()
      .subscribe((res: any) => {
        this.countries = res;
        this.logger.debug(`PlaceEditComponent getCountries() ${this.countries.length} items`);
      }, err => {
        this.logger.error(err);
      });

    this.formData = this.formBuilder.group({
      name: [null, Validators.required],
      areaCode: [null, Validators.required],
      locationType: [null, Validators.required],
      authScope: [null, Validators.required],
      summary: [null],
      notes: [null],
      coordinatesStr: [null],
      primaryUrl: [null],
      imageUrl: [null],
      tags: this.formBuilder.array([])
    });

    this.locationTypes = this.masterData.getLocationTypes();
    this.authScopes = this.masterData.getList(ListType.AUTH_SCOPE);
    // this.loadFiles();
  }

  // get initial value of selecbox base on enum value provided by backend
  getSelectedLotype(): ListItem {
    return this.masterData.lookupLocationType(this.formData.get('locationType').value);
  }

  getSelectedAuthScope(): ListItem {
    return this.masterData.getListItem(ListType.AUTH_SCOPE, this.formData.get('authScope').value);
  }

  getPlace(id: any) {
    this.api.getPlace(id).subscribe((data: any) => {
      this.id = data.id;
      // use patch not set, avoid
      // https://stackoverflow.com/questions/51047540/angular-reactive-form-error-must-supply-a-value-for-form-control-with-name
      this.formData.patchValue({
        name: data.name,
        summary: data.summary,
        notes: data.notes,
        areaCode: data.areaCode,
        imageUrl: data.imageUrl,
        primaryUrl: data.primaryUrl,
        locationType: data.locationType,
        authScope: data.authScope,
        coordinatesStr: (Array.isArray((data.coordinates)) && (data.coordinates.length > 1)) ? `${data.coordinates[1]} ${data.coordinates[0]}` : null
      });
      // patch didn't work for form array, this workaround doess ..
      // https://www.cnblogs.com/Answer1215/p/7376987.html [Angular] Update FormArray with patchValue
      if (data.tags) {
        for (const item of data.tags) {
          (this.formData.get('tags') as FormArray).push(new FormControl(item));
        }
      }
    });
  }

  addTag(e: MatChipInputEvent) {
    const input = e.input;
    const value = e.value;
    if ((value || '').trim()) {
      const control = this.formData.controls.tags as FormArray;
      control.push(this.formBuilder.control(value.trim().toLowerCase()));
    }
    if (input) {
      input.value = '';
    }
  }

  removeTag(i: number) {
    const control = this.formData.controls.tags as FormArray;
    control.removeAt(i);
  }

  // Copying any text passed here
  checkCoordinates(event: any) {
    const geostr = this.formData.value.coordinatesStr;
    try {
      const newval = this.parseCoordinates( this.formData.value.coordinatesStr);
      this.formData.patchValue( {coordinatesStr: newval });
    } catch (e) {
      this.logger.warn(e.message);
      this.snackBar.open(e.message);
    }
  }

  parseCoordinates(mapsurl: string): string {
    const regexpCoordinates = /(-?[0-9\.]+)[,\s]+(-?[0-9\.]+)/;
    const match = mapsurl.match(regexpCoordinates);
    if (match == null) {
      throw Error(mapsurl + 'does not match ' + regexpCoordinates);
    }
    return `${match[1]},${match[2]}`;
  }
  onFormSubmit() {
    const place = this.formData.value;
    // Todo: validate update coordindates array after they've been entered, not shortly before submit
    if (place.coordinatesStr) {
      const sco = new SmartCoordinates((place.coordinatesStr));
      place.coordinates = sco.lonLatArray;
      this.logger.debug('coordinates', sco);
      delete place.coordinatesStr;
    }
    this.logger.debug('submit()', place);
    this.api.updatePlace(this.id, this.formData.value)
      .subscribe((res: any) => {
          this.snackBar.open('Place has been successfully updated', 'Close');
          const id = res.id;
          this.router.navigate(['/place-details', id]);
        }, (err: any) => {
          this.logger.error(err);
        }
      );
  }

  placeDetails() {
    this.router.navigate(['/place-details', this.id]);
  }

}
