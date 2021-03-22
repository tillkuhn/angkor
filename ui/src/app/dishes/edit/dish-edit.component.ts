import {Component, OnInit} from '@angular/core';
import {Area} from '../../domain/area';
import {ListItem} from '../../domain/list-item';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {ApiService} from '@shared/services/api.service';
import {FileService} from '@shared/services/file.service';
import {NGXLogger} from 'ngx-logger';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '@shared/services/auth.service';
import {ListType, MasterDataService} from '@shared/services/master-data.service';
import {EntityType} from '../../domain/entities';
import {ApiHelper} from '@shared/helpers/api-helper';
import {DishStoreService} from '../dish-store.service';
import {Dish} from '../../domain/dish';

@Component({
  selector: 'app-dish-edit',
  templateUrl: './dish-edit.component.html',
  styleUrls: ['../../shared/components/common.component.scss']
})
export class DishEditComponent implements OnInit {

  countries: Area[] = [];
  authScopes: ListItem[];
  formData: FormGroup;
  id = '';
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];   // For Tag support
  matcher = new DefaultErrorStateMatcher();

  constructor(private api: ApiService,
              public store: DishStoreService,
              private fileService: FileService,
              private formBuilder: FormBuilder,
              private logger: NGXLogger,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              public authService: AuthService,
              public masterData: MasterDataService) {
  }

  ngOnInit() {
    this.loadItem(this.route.snapshot.params.id);

    this.masterData.countries
      .subscribe((res: any) => {
        this.countries = res;
        this.logger.debug(`DishEditComponent getCountries() ${this.countries.length} codes`);
      }, err => {
        this.logger.error(err);
      });

    this.formData = this.formBuilder.group({
      name: [null, Validators.required],
      areaCode: [null, Validators.required],
      authScope: [null, Validators.required],
      rating: [null, Validators.pattern('^[0-9]*$')],
      summary: [null],
      notes: [null],
      primaryUrl: [null],
      imageUrl: [null],
      tags: this.formBuilder.array([])
    });

    this.authScopes = this.masterData.getList(ListType.AUTH_SCOPE);
  }

  getSelectedAuthScope(): ListItem {
    return this.masterData.getListItem(ListType.AUTH_SCOPE, this.formData.get('authScope').value);
  }

  loadItem(id: any) {
    this.store.getItem(id).subscribe((data: Dish) => {
      this.id = data.id;
      // use patch on the reactive form data, not set. See
      // https://stackoverflow.com/questions/51047540/angular-reactive-form-error-must-supply-a-value-for-form-control-with-name
      this.formData.patchValue({
        name: data.name,
        summary: data.summary,
        notes: data.notes,
        areaCode: data.areaCode,
        imageUrl: data.imageUrl,
        primaryUrl: data.primaryUrl,
        authScope: data.authScope,
        rating: data.rating
      });
      // patch didn't work if the form is an array, this workaround does. See
      // https://www.cnblogs.com/Answer1215/p/7376987.html [Angular] Update FormArray with patchValue
      if (data.tags) {
        for (const item of data.tags) {
          (this.formData.get('tags') as FormArray).push(new FormControl(item));
        }
      }
    });
  }

  // star rating does not support reactive form, so we need to compensate
  ratingOuput(newRating: number) {
    this.logger.info(`DishEditComponent: Rating set to ${newRating}`);
    this.formData.patchValue({rating: newRating});
  }


  // Receive event from child if image is selected https://fireship.io/lessons/sharing-data-between-angular-components-four-methods/
  receiveImageMessage($event) {
    this.logger.info(`Received image event ${$event} from child component`);
    const newImageUrl = $event;
    if (this.formData.value.imageUrl === newImageUrl) {
      this.snackBar.open(`This image is already set as title`, 'Close');
    } else {
      this.formData.patchValue({imageUrl: newImageUrl});
      this.snackBar.open(`Set new title image: ${newImageUrl} `, 'Close');
    }
  }

  onFormSubmit() {
    const item = this.formData.value;
    this.logger.trace(`Received ${JSON.stringify(item)} from API`);
    this.store.updateItem(this.id, this.formData.value)
      .subscribe((res: any) => {
          // 'Dish has been updated, Bon Appétit!'
          this.navigateToItemDetails(res.id);
        }, (err: any) => {
          this.logger.error(err);
        }
      );
  }

  navigateToItemDetails(id = this.id) {
    const entityPath = ApiHelper.getApiPath(EntityType.Dish);
    this.router.navigate([`/${entityPath}/details`, id]);
  }

}
