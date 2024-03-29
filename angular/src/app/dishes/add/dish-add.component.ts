import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Area} from '@app/domain/area';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {NGXLogger} from 'ngx-logger';
import {DEFAULT_AUTH_SCOPE, MasterDataService} from '@shared/services/master-data.service';
import {Router} from '@angular/router';
import {EntityType} from '@shared/domain/entities';
import {ApiHelper} from '@shared/helpers/api-helper';
import {DishStoreService} from '../dish-store.service';

@Component({
  selector: 'app-dish-add',
  templateUrl: './dish-add.component.html',
  styleUrls: []
})
export class DishAddComponent implements OnInit {

  countries$: Observable<Array<Area>>;
  formData: UntypedFormGroup;
  matcher = new DefaultErrorStateMatcher();

  constructor(private store: DishStoreService,
              private formBuilder: UntypedFormBuilder,
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
    this.store.addItem({
      ...this.formData.value,
      authScope: DEFAULT_AUTH_SCOPE // default value should be rather restricted
    })
      .subscribe((res: any) => {
        const id = res.id;
        const entityPath = ApiHelper.getApiPath(EntityType.Dish);
        this.router.navigate([`/${entityPath}/edit`, id]).then(); // swallow returned promise
      }, (err: any) => {
        this.logger.error(err);
      });
  }

}
