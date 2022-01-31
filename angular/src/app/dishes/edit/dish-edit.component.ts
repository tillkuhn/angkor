import {Component, OnInit} from '@angular/core';
import {Area} from '@app/domain/area';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {ImagineService} from '@shared/modules/imagine/imagine.service';
import {NGXLogger} from 'ngx-logger';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '@shared/services/auth.service';
import {ListType, MasterDataService} from '@shared/services/master-data.service';
import {EntityType} from '@shared/domain/entities';
import {ApiHelper} from '@shared/helpers/api-helper';
import {DishStoreService} from '../dish-store.service';
import {Dish} from '@app/domain/dish';
import {ListItem} from '@shared/domain/list-item';
import {FileEvent} from '@shared/modules/imagine/file-event';
import {FileItem} from '@shared/modules/imagine/file-item';
import {NotificationService} from '@shared/services/notification.service';

@Component({
  selector: 'app-dish-edit',
  templateUrl: './dish-edit.component.html',
  styleUrls: ['../../shared/components/common.component.scss']
})
export class DishEditComponent implements OnInit {

  private readonly className = 'DishEditComponent';

  countries: Area[] = [];
  authScopes: ListItem[];
  formData: FormGroup;
  id = '';
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];   // For Tag support
  matcher = new DefaultErrorStateMatcher();

  constructor(public store: DishStoreService,
              private fileService: ImagineService,
              private formBuilder: FormBuilder,
              private logger: NGXLogger,
              private route: ActivatedRoute,
              private router: Router,
              private notifications: NotificationService,
              public authService: AuthService,
              public masterData: MasterDataService) {
  }

  ngOnInit() {
    this.loadItem(this.route.snapshot.params.id);

    this.masterData.countries
      .subscribe({
        next: (res: any) => {
          this.countries = res;
          this.logger.debug(`DishEditComponent getCountries() ${this.countries.length} codes`);
        }, error: err => {
          this.logger.error(err);
        }
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
  onRatingUpdateEvent(newRating: number) {
    this.logger.info(`DishEditComponent: Rating set to ${newRating}`);
    this.formData.patchValue({rating: newRating});
  }


  // Receive event from child if image is selected https://fireship.io/lessons/sharing-data-between-angular-components-four-methods/
  // TODO duplicate code, should share with placeEditComponent
  onImagineEvent(event: FileEvent) {
    const defaultSuffix = '?large';
    this.logger.info(`${this.className}.onImagineEvent: Received image ${event.subject} event with data=${event.data}`);
    if (event.subject === 'SELECTED') {
      const imageUrl = (event.data as string) + defaultSuffix; // data is the full path (string) e.g. /imagine/..../bla.jpg?large
      if (this.formData.value.imageUrl === imageUrl) {
        this.notifications.warn(`This image is already set as title`);
      } else {
        this.formData.patchValue({imageUrl: imageUrl});
        this.notifications.success(`Set new title image: ${imageUrl} `);
      }

    } else if (event.subject === 'ACKNOWLEDGED') {
      // data is the body of the server response
      this.logger.debug(`${this.className}.onImagineEvent: Recent upload acknowledged`);

    } else if (event.subject === 'LIST_REFRESH') {
      const files = event.data as FileItem[] // data type for LIST_REFRESH events
      if (! this.formData.get('imageUrl').value && files?.length > 0) {
        this.logger.debug(`${this.className}.onImagineEvent: Current imageUrl is empty, auto assign first item from list`);
        this.formData.patchValue({imageUrl: files[0].path + defaultSuffix});
      } else {
        this.logger.debug(`${this.className}.onImagineEvent: File list was refreshed, but current ImageUrl is already set`);
      }

    } else {
      this.logger.debug(`${this.className}.onImagineEvent: No further action required for event ${event.subject}`);
    }
  }

  onFormSubmit() {
    const item = this.formData.value;
    this.logger.trace(`Received ${JSON.stringify(item)} from API`);
    this.store.updateItem(this.id, this.formData.value)
      .subscribe({
        next: (res: any) => {
          this.navigateToItemDetails(res.id);           // 'Dish has been updated, Bon Appétit!'
        }, error: (err: any) => {
          this.logger.error(err);
        }
      });
  }

  navigateToItemDetails(id = this.id) {
    const entityPath = ApiHelper.getApiPath(EntityType.Dish);
    this.router.navigate([`/${entityPath}/details`, id]).then(); // then() only required to avoid warning
  }

}
