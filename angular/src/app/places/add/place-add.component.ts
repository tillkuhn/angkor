import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {NGXLogger} from 'ngx-logger';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {Observable} from 'rxjs';
import {Area} from '@app/domain/area';
import {DEFAULT_AUTH_SCOPE, MasterDataService} from '@shared/services/master-data.service';
import {EntityType} from '@shared/domain/entities';
import {ApiHelper} from '@shared/helpers/api-helper';
import {PlaceStoreService} from '../place-store.service';

@Component({
  selector: 'app-place-add',
  templateUrl: './place-add.component.html',
  styleUrls: ['../../shared/components/common.component.scss']
})
export class PlaceAddComponent implements OnInit {

  countries$: Observable<Array<Area>>;
  formData: UntypedFormGroup;
  matcher = new DefaultErrorStateMatcher();

  constructor(private store: PlaceStoreService,
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
      .subscribe({
        next: (res: any) => {
          const id = res.id;
          const entityPath = ApiHelper.getApiPath(EntityType.Place);
          // trailing then() avoids the intellij warning b/c of returned promise
          this.router.navigate([`/${entityPath}/edit`, id]).then();
        }, error: (err: any) => {
          this.logger.error(err);
        }
      });
  }

}

