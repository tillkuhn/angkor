import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Area} from '../../domain/area';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {DefaultErrorStateMatcher} from '../../shared/form-helper';
import {ApiService} from '../../shared/api.service';
import {NGXLogger} from 'ngx-logger';
import {MasterDataService} from '../../shared/master-data.service';
import {Router} from '@angular/router';
import {EntityType} from '../../domain/entities';

@Component({
  selector: 'app-dish-add',
  templateUrl: './dish-add.component.html',
  styleUrls: ['./dish-add.component.scss']
})
export class DishAddComponent implements OnInit {

  countries$: Observable<Array<Area>>;
  formData: FormGroup;
  matcher = new DefaultErrorStateMatcher();

  constructor(private api: ApiService,
              private formBuilder: FormBuilder,
              private logger: NGXLogger,
              private masterDataService: MasterDataService,
              private router: Router) {
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
    this.api.addDish({
      ...this.formData.value,
      authScope: 'ALL_AUTH' // set to all_auth by default
    })
      .subscribe((res: any) => {
        const id = res.id;
        const entityPath = ApiService.getApiPath(EntityType.DISH);
        this.router.navigate([`/${entityPath}/edit`, id]);
      }, (err: any) => {
        this.logger.error(err);
      });
  }

}
