import {Component, OnInit} from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {ApiService} from '../shared/api.service';
import {FormControl, FormGroupDirective, FormBuilder, FormGroup, NgForm, Validators} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';
import {NGXLogger} from 'ngx-logger';
import {Area} from '../domain/area';
import {MyErrorStateMatcher} from '../shared/form-helper';
import {MasterDataService} from '../shared/master-data.service';
import {LocationType} from '../domain/place';
import {ListItem} from '../domain/shared';

@Component({
  selector: 'app-place-edit',
  templateUrl: './place-edit.component.html',
  styleUrls: ['./place-edit.component.scss']
})
export class PlaceEditComponent implements OnInit {
  countries: Area[] = [];
  locationTypes: ListItem[];
  coordinates: string;
  placeForm: FormGroup;
  id = '';
  matcher = new MyErrorStateMatcher();

  constructor(private router: Router, private route: ActivatedRoute,
              private api: ApiService, private formBuilder: FormBuilder,
              private logger: NGXLogger, private masterData: MasterDataService) {
  }

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
      notes: [''],
      areaCode: [null, Validators.required],
      primaryUrl: [''],
      imageUrl: [null, Validators.required],
      locationType: [null, Validators.required],
    });
    this.locationTypes = this.masterData.getLocationTypes();

  }

  getPlace(id: any) {
    this.api.getPlace(id).subscribe((data: any) => {
      this.id = data.id;
      this.placeForm.patchValue({
        name: data.name,
        summary: data.summary,
        notes: data.notes,
        areaCode: data.areaCode,
        imageUrl: data.imageUrl,
        primaryUrl: data.primaryUrl,
        locationType: data.locationType
      });
    });
  }

  onFormSubmit() {
    this.logger.info('submit()', this.placeForm.value);
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
