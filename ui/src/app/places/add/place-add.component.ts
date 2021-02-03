import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ApiService} from '../../shared/api.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NGXLogger} from 'ngx-logger';
import {DefaultErrorStateMatcher} from '../../shared/form-helper';
import {Observable} from 'rxjs';
import {Area} from '../../domain/area';
import {DEFAULT_AUTH_SCOPE, MasterDataService} from '../../shared/master-data.service';
import {EntityType} from '../../domain/entities';

@Component({
  selector: 'app-place-add',
  templateUrl: './place-add.component.html',
  styleUrls: ['./place-add.component.scss']
})
export class PlaceAddComponent implements OnInit {

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
    this.api.addPlace({
      ...this.formData.value,
      authScope: DEFAULT_AUTH_SCOPE // default value should be rather restricted
    })
      .subscribe((res: any) => {
        const id = res.id;
        const entityPath = ApiService.getApiPath(EntityType.PLACE);
        this.router.navigate([`/${entityPath}/edit`, id]);
      }, (err: any) => {
        this.logger.error(err);
      });
  }

}

