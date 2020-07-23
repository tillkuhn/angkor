import {Component, OnInit} from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {ApiService} from '../api.service';
import {FormControl, FormGroupDirective, FormBuilder, FormGroup, NgForm, Validators} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';
import {NGXLogger} from 'ngx-logger';
import {Area} from '../domain/area';
import {LocationType, LOCATION_TYPES} from '../domain/place';
import {MyErrorStateMatcher} from '../shared/form-helper';


/*
interface SelectValue {
  value: string;
  viewValue: string;
}
 */

@Component({
  selector: 'app-place-edit',
  templateUrl: './place-edit.component.html',
  styleUrls: ['./place-edit.component.scss']
})
export class PlaceEditComponent implements OnInit {
  countries: Area[] = [];
  locationTypes: LocationType[] = []

  placeForm: FormGroup;
  id = '';
  isLoadingResults = false;
  matcher = new MyErrorStateMatcher();

  constructor(private router: Router, private route: ActivatedRoute,
              private api: ApiService, private formBuilder: FormBuilder,
              private logger: NGXLogger) {
  }

  getSelectedLotype(): LocationType {
    return LOCATION_TYPES[this.placeForm.get('locationType').value];
  }

  ngOnInit() {
    this.getPlace(this.route.snapshot.params.id);
    this.api.getCountries()
      .subscribe((res: any) => {
        this.countries = res;
        this.logger.debug('getCountries()', this.countries);
        this.isLoadingResults = false;
      }, err => {
        this.logger.error(err);
        this.isLoadingResults = false;
      });

    this.placeForm = this.formBuilder.group({
      name: [null, Validators.required],
      summary: [null, Validators.required],
      areaCode: [null, Validators.required],
      imageUrl: [null, Validators.required],
      locationType: [null, Validators.required],
    });

    for (const key in LOCATION_TYPES) {
      // tslint complains for (... in ...) statements must be filtered with an if statement
      if (LOCATION_TYPES.hasOwnProperty(key)) {
        this.locationTypes.push(LOCATION_TYPES[key]);
      }
    }
  }

  getPlace(id: any) {
    this.api.getPlace(id).subscribe((data: any) => {
      this.id = data.id;
      this.placeForm.setValue({
        name: data.name,
        summary: data.summary,
        areaCode: data.areaCode,
        imageUrl: data.imageUrl,
        locationType: data.locationType
      });
    });
  }

  onFormSubmit() {
    this.isLoadingResults = true;
    this.logger.info( this.placeForm.value)
    this.api.updatePlace(this.id, this.placeForm.value)
      .subscribe((res: any) => {
          const id = res.id;
          this.isLoadingResults = false;
          this.router.navigate(['/place-details', id]);
        }, (err: any) => {
          this.logger.error(err);
          this.isLoadingResults = false;
        }
      );
  }

  placeDetails() {
    this.router.navigate(['/place-details', this.id]);
  }

}
