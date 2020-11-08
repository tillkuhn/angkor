import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ApiService} from '../../shared/api.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NGXLogger} from 'ngx-logger';
import {MyErrorStateMatcher} from '../../shared/form-helper';
import {Observable} from 'rxjs';
import {Area} from '../../domain/area';
import {MasterDataService} from '../../shared/master-data.service';


@Component({
  selector: 'app-place-add',
  templateUrl: './place-add.component.html',
  styleUrls: ['./place-add.component.scss']
})
export class PlaceAddComponent implements OnInit {

  countries$: Observable<Array<Area>>;
  formData: FormGroup;
  matcher = new MyErrorStateMatcher();
  areaCode= '';

  constructor(private router: Router,
              private api: ApiService,
              private masterDataService: MasterDataService,
              private logger: NGXLogger,
              private formBuilder: FormBuilder) {
  }

  ngOnInit() {
    this.countries$ = this.masterDataService.countries;
    this.formData = this.formBuilder.group({
      name: [null, [Validators.required, Validators.minLength(3)]],
      areaCode: [null, Validators.required],
    });
  }

  // Create place with mandatory fields, on success goto edit mode
  onFormSubmit() {
    this.masterDataService.forceReload();
    this.api.addPlace(this.formData.value)
      .subscribe((res: any) => {
        const id = res.id;
        this.router.navigate(['/place-edit', id]);
      }, (err: any) => {
        this.logger.error(err);
      });
  }

}

