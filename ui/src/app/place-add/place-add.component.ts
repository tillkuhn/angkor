import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ApiService} from '../shared/api.service';
import {FormControl, FormGroupDirective, FormBuilder, FormGroup, NgForm, Validators} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';
import {NGXLogger} from 'ngx-logger';
import {MyErrorStateMatcher} from '../shared/form-helper';
import {Observable} from 'rxjs';
import {Area} from '../domain/area';
import {MasterDataService} from '../shared/master-data.service';


@Component({
  selector: 'app-place-add',
  templateUrl: './place-add.component.html',
  styleUrls: ['./place-add.component.scss']
})
export class PlaceAddComponent implements OnInit {

  countries$: Observable<Array<Area>>;
  placeForm: FormGroup;
  matcher = new MyErrorStateMatcher();

  constructor(private router: Router, private api: ApiService,
              private masterDataService: MasterDataService,private logger: NGXLogger, private formBuilder: FormBuilder) {
  }

  ngOnInit() {
    this.countries$ = this.masterDataService.countries;
    this.placeForm = this.formBuilder.group({
      name: [null, Validators.required],
      summary: [null, Validators.required],
      areaCode: [null, Validators.required],
      imageUrl: [null]
    });
  }

  onFormSubmit() {
    this.masterDataService.invalidateCountries();
    this.api.addPlace(this.placeForm.value)
      .subscribe((res: any) => {
        const id = res.id;
        this.router.navigate(['/place-details', id]);
      }, (err: any) => {
        this.logger.error(err);
      });
  }

}

