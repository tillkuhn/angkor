import {Component, OnInit} from '@angular/core';
import {Area} from '../../domain/area';
import {ListItem} from '../../domain/list-item';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {DefaultErrorStateMatcher} from '../../shared/form-helper';
import {ApiService} from '../../shared/api.service';
import {FileService} from '../../shared/file.service';
import {NGXLogger} from 'ngx-logger';
import {ActivatedRoute, Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '../../shared/auth.service';
import {ListType, MasterDataService} from '../../shared/master-data.service';
import {MatChipInputEvent} from '@angular/material/chips';
import {SmartCoordinates} from '../../domain/smart-coordinates';
import {EntityType} from '../../domain/entities';

@Component({
  selector: 'app-dish-edit',
  templateUrl: './dish-edit.component.html',
  styleUrls: ['./dish-edit.component.scss']
})
export class DishEditComponent implements OnInit {

  countries: Area[] = [];
  authScopes: ListItem[];
  formData: FormGroup;
  id = '';
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];   // For Tag support
  matcher = new DefaultErrorStateMatcher();

  constructor(private api: ApiService,
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

    this.api.getCountries()
      .subscribe((res: any) => {
        this.countries = res;
        this.logger.debug(`DishEditComponent getCountries() ${this.countries.length} items`);
      }, err => {
        this.logger.error(err);
      });

    this.formData = this.formBuilder.group({
      name: [null, Validators.required],
      areaCode: [null, Validators.required],
      authScope: [null, Validators.required],
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
    this.api.getDish(id).subscribe((data: any) => {
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

  addTag(e: MatChipInputEvent) {
    const input = e.input;
    const value = e.value;
    if ((value || '').trim()) {
      const control = this.formData.controls.tags as FormArray;
      control.push(this.formBuilder.control(value.trim().toLowerCase()));
    }
    if (input) {
      input.value = '';
    }
  }

  removeTag(i: number) {
    const control = this.formData.controls.tags as FormArray;
    control.removeAt(i);
  }

  onFormSubmit() {
    const item = this.formData.value;
    this.logger.debug('DishEditComponent.submit', item);
    this.api.updateDish(this.id, this.formData.value)
      .subscribe((res: any) => {
          this.snackBar.open('Dish has been updated, Bon Appétit!', 'Close');
          this.navigateToItemDetails(res.id);
        }, (err: any) => {
          this.logger.error(err);
        }
      );
  }

  navigateToItemDetails(id = this.id) {
    const entityPath = ApiService.getApiPath(EntityType.DISH);
    this.router.navigate([`/${entityPath}/details`, id]);
  }

}
