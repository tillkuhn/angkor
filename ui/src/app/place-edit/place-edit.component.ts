import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ApiService} from '../shared/api.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NGXLogger} from 'ngx-logger';
import {Area} from '../domain/area';
import {MyErrorStateMatcher} from '../shared/form-helper';
import {MasterDataService} from '../shared/master-data.service';
import {ListItem} from '../domain/shared';
import {SmartCoordinates} from '../domain/smart-coordinates';

@Component({
  selector: 'app-place-edit',
  templateUrl: './place-edit.component.html',
  styleUrls: ['./place-edit.component.scss']
})
export class PlaceEditComponent implements OnInit {
  countries: Area[] = [];
  locationTypes: ListItem[];
  placeForm: FormGroup;
  id = '';
  matcher = new MyErrorStateMatcher();

  constructor(private router: Router, private route: ActivatedRoute,
              private api: ApiService, private formBuilder: FormBuilder,
              private logger: NGXLogger, private masterData: MasterDataService) {
  }

  // get initial value of selecbox base on enum value provided by backend
  getSelectedLotype(): ListItem {
    return this.masterData.lookupLocationType(this.placeForm.get('locationType').value);
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

    this.placeForm = this.formBuilder.group({
      name: [null, Validators.required],
      summary: [null, Validators.required],
      notes: [null],
      coordinatesStr: [null],
      areaCode: [null, Validators.required],
      primaryUrl: [null],
      imageUrl: [null, Validators.required],
      locationType: [null, Validators.required],
    });
    this.locationTypes = this.masterData.getLocationTypes();

  }

  getPlace(id: any) {
    this.api.getPlace(id).subscribe((data: any) => {
      this.id = data.id;
      // use patch not set, avoid
      // https://stackoverflow.com/questions/51047540/angular-reactive-form-error-must-supply-a-value-for-form-control-with-name
      this.placeForm.patchValue({
        name: data.name,
        summary: data.summary,
        notes: data.notes,
        areaCode: data.areaCode,
        imageUrl: data.imageUrl,
        primaryUrl: data.primaryUrl,
        locationType: data.locationType,
        coordinatesStr:  (Array.isArray((data.coordinates))  && (data.coordinates.length > 1)) ? `${data.coordinates[1]} ${data.coordinates[0]}` : null
      });
    });
  }

  onFormSubmit() {
    const place = this.placeForm.value;
    if (place.coordinatesStr) {
      const sco = new SmartCoordinates((place.coordinatesStr));
      place.coordinates = sco.lonLatArray;
      this.logger.debug('coordinates',sco);
      delete place.coordinatesStr;
    }
    this.logger.info('submit()', place);
    this.api.updatePlace(this.id, this.placeForm.value)
      .subscribe((res: any) => {
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
