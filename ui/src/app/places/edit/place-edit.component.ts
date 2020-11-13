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
import {EntityType} from '../../domain/common';
import {Note} from '../../domain/note';
import {FileItem} from '../../domain/file-item';

@Component({
  selector: 'app-place-edit',
  templateUrl: './place-edit.component.html',
  styleUrls: ['./place-edit.component.scss']
})
export class PlaceEditComponent implements OnInit {
  fileColumns: string[] = ['filename', 'tags', 'size'];
  countries: Area[] = [];
  locationTypes: ListItem[];
  authScopes: ListItem[];
  formData: FormGroup;
  id = '';
  files: FileItem[] = [];
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];   // For Tag support
  matcher = new MyErrorStateMatcher();

  // https://medium.com/@altissiana/how-to-pass-a-function-to-a-child-component-in-angular-719fc3d1ee90
  refreshCallback = (args: any): void => {
    this.loadFiles();
  }

  constructor(private router: Router,
              private route: ActivatedRoute,
              private api: ApiService,
              private fileService: FileService,
              private formBuilder: FormBuilder,
              private snackBar: MatSnackBar,
              public authService: AuthService,
              private logger: NGXLogger,
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
      summary: [null, Validators.required],
      notes: [null],
      coordinatesStr: [null],
      areaCode: [null, Validators.required],
      primaryUrl: [null],
      imageUrl: [null, Validators.required],
      locationType: [null, Validators.required],
      authScope: [null, Validators.required],
      tags: this.formBuilder.array([])
    });

    this.locationTypes = this.masterData.getLocationTypes();
    this.authScopes = this.masterData.getList(ListType.AUTH_SCOPE);
    this.loadFiles();
  }

  // get initial value of selecbox base on enum value provided by backend
  getSelectedLotype(): ListItem {
    return this.masterData.lookupLocationType(this.formData.get('locationType').value);
  }

  // https://medium.com/@altissiana/how-to-pass-a-function-to-a-child-component-in-angular-719fc3d1ee90
  loadFiles() {
    this.fileService.getEntityFiles(EntityType.PLACE, this.route.snapshot.params.id)
      .subscribe((res: FileItem[]) => {
        this.files = res;
        this.logger.debug('getFiles()', this.files.length);
      }, err => {
        this.logger.error(err);
      });
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
