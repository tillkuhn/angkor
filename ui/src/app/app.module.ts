import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {BrowserModule} from '@angular/platform-browser';
import {DishesComponent} from './dishes/list/dishes.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HomeComponent} from './home/home.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {LayoutModule} from '@angular/cdk/layout';
import {LoggerModule, NgxLoggerLevel} from 'ngx-logger';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MapComponent} from './map/map.component';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatChipsModule} from '@angular/material/chips';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatMenuModule} from '@angular/material/menu';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSelectModule} from '@angular/material/select';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MAT_SNACK_BAR_DEFAULT_OPTIONS, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatSortModule} from '@angular/material/sort';
import {MatTableModule} from '@angular/material/table';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MomentModule} from 'ngx-moment';
import {NgModule} from '@angular/core';
import {NgxMapboxGLModule} from 'ngx-mapbox-gl';
import {NotesComponent} from './notes/notes.component';
import {PlaceAddComponent} from './place-add/place-add.component';
import {PlaceDetailComponent} from './place-detail/place-detail.component';
import {PlaceEditComponent} from './place-edit/place-edit.component';
import {PlacesComponent} from './places/places.component';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatNativeDateModule} from '@angular/material/core';
import {LoadingInterceptor} from './shared/loading.interceptor';
import {MatDialogModule} from '@angular/material/dialog';
import {ConfirmDialogComponent} from './shared/components/confirm-dialog/confirm-dialog.component';
import {MetricsComponent} from './admin/metrics/metrics.component';
import {AreaDisplayComponent} from './shared/components/area-display/area-display.component';
import {UserProfileComponent} from './user-profile/user-profile.component';
import {DishAddComponent} from './dishes/add/dish-add.component';
import { DishDetailComponent } from './dishes/detail/dish-detail.component';
import { DishEditComponent } from './dishes/edit/dish-edit.component';
import { AreaTreeComponent } from './area-tree/area-tree.component';
import {MatTreeModule} from '@angular/material/tree';
import { FileUploadComponent } from './file-upload/file-upload.component';


//  imports makes the exported declarations of other modules available in the current module
//  declarations are to make directives (including components and pipes) from the current module available to other
//    directives in the current module. Selectors of directives components or pipes are only matched against the HTML
//   if they are declared or imported.
//  providers are to make services and values known to DI (dependency injection). They are added to the root scope and
// they are injected to other services or directives that have them as dependency.
@NgModule({
  declarations: [
    AppComponent,
    PlacesComponent,
    PlaceDetailComponent,
    PlaceAddComponent,
    PlaceEditComponent,
    MapComponent,
    HomeComponent,
    DishesComponent,
    NotesComponent,
    ConfirmDialogComponent,
    MetricsComponent,
    AreaDisplayComponent,
    UserProfileComponent,
    DishAddComponent,
    DishDetailComponent,
    DishEditComponent,
    AreaTreeComponent,
    FileUploadComponent
  ],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    HttpClientModule,
    LayoutModule,
    LayoutModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatNativeDateModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatSidenavModule,
    MatSnackBarModule,
    MatSortModule,
    MatTreeModule,
    MatTableModule,
    MatToolbarModule,
    NgxMapboxGLModule,
    ReactiveFormsModule,
    LoggerModule.forRoot({
      // serverLoggingUrl: '/api/logs',
      level: NgxLoggerLevel.DEBUG,
      // serverLogLevel: NgxLoggerLevel.ERROR,
      disableConsoleLogging: false
    }),
    // https://www.npmjs.com/package/ngx-moment
    MomentModule.forRoot({
      relativeTimeThresholdOptions: {
        m: 59
      }
    }),
    MatSelectModule,
    MatDatepickerModule,
    MatDialogModule
  ],
  exports: [],
  providers: [
    // intercept all http requests for progess (aka loading) bar
    {provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true},
    // default duration for snackbar messages
    // https://material.angular.io/components/snack-bar/overview#setting-the-global-configuration-defaults
    {provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: {duration: 2500}}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
