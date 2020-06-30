import {Component, OnInit} from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {ApiService} from '../api.service';
import {FormControl, FormGroupDirective, FormBuilder, FormGroup, NgForm, Validators} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';
import {NGXLogger} from "ngx-logger";
import {Geocode} from '../domain/geocode';
import {LocationType, LOCATION_TYPES} from '../domain/place';

export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const isSubmitted = form && form.submitted;
    return !!(control && control.invalid && (control.dirty || control.touched || isSubmitted));
  }
}

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
  countries: Geocode[] = [];
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
    return LOCATION_TYPES[this.placeForm.get('lotype').value];
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
      country: [null, Validators.required],
      imageUrl: [null, Validators.required],
      lotype: [null, Validators.required],
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
        country: data.country,
        imageUrl: data.imageUrl,
        lotype: data.lotype
      });
    });
  }

  onFormSubmit() {
    this.isLoadingResults = true;
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
