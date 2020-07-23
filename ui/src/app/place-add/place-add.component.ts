import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ApiService} from '../api.service';
import {FormControl, FormGroupDirective, FormBuilder, FormGroup, NgForm, Validators} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';
import {NGXLogger} from 'ngx-logger';
import {MyErrorStateMatcher} from '../shared/form-helper';


@Component({
  selector: 'app-place-add',
  templateUrl: './place-add.component.html',
  styleUrls: ['./place-add.component.scss']
})
export class PlaceAddComponent implements OnInit {

  placeForm: FormGroup;
  name = '';
  summary = '';
  country = '';
  // price: number = null;
  isLoadingResults = false;
  matcher = new MyErrorStateMatcher();

  constructor(private router: Router, private api: ApiService, private logger: NGXLogger, private formBuilder: FormBuilder) {
  }

  ngOnInit() {
    this.placeForm = this.formBuilder.group({
      name: [null, Validators.required],
      summary: [null, Validators.required],
      country: [null, Validators.required],
    });
  }

  onFormSubmit() {
    this.isLoadingResults = true;
    this.api.addPlace(this.placeForm.value)
      .subscribe((res: any) => {
        const id = res.id;
        this.isLoadingResults = false;
        this.router.navigate(['/place-details', id]);
      }, (err: any) => {
        this.logger.error(err);
        this.isLoadingResults = false;
      });
  }

}

