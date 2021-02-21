import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Area} from '../../domain/area';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {DefaultErrorStateMatcher} from '../../shared/form-helper';
import {ApiService} from '../../shared/api.service';
import {NGXLogger} from 'ngx-logger';
import {DEFAULT_AUTH_SCOPE, MasterDataService} from '../../shared/master-data.service';
import {Router} from '@angular/router';
import {EntityType} from '../../domain/entities';
import {EntityHelper} from '../../entity-helper';
import {DishStoreService} from '../dish-store.service';

@Component({
  selector: 'app-dish-add',
  templateUrl: './dish-add.component.html',
  styleUrls: ['./dish-add.component.scss']
})
export class DishAddComponent implements OnInit {

  countries$: Observable<Array<Area>>;
  formData: FormGroup;
  matcher = new DefaultErrorStateMatcher();

  // areaCode = '';

  constructor(private store: DishStoreService,
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
    this.store.addItem({
      ...this.formData.value,
      authScope: DEFAULT_AUTH_SCOPE // default value should be rather restricted
    })
      .subscribe((res: any) => {
        const id = res.id;
        const entityPath = EntityHelper.getApiPath(EntityType.Dish);
        this.router.navigate([`/${entityPath}/edit`, id]);
      }, (err: any) => {
        this.logger.error(err);
      });
  }

}
