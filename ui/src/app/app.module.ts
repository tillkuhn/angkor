import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {AreaDisplayComponent} from './shared/components/area-display/area-display.component';
import {AreaTreeComponent} from './area-tree/area-tree.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {BrowserModule} from '@angular/platform-browser';
import {BytesizePipe} from './shared/pipes/bytesize.pipe';
import {ConfirmDialogComponent} from './shared/components/confirm-dialog/confirm-dialog.component';
import {DateFnsModule} from 'ngx-date-fns';
import {DishAddComponent} from './dishes/add/dish-add.component';
import {DishDetailComponent} from './dishes/detail/dish-detail.component';
import {DishEditComponent} from './dishes/edit/dish-edit.component';
import {DishesComponent} from './dishes/list/dishes.component';
import {FileInputDialogComponent, FileUploadComponent} from './shared/components/file-upload/file-upload.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {HomeComponent} from './home/home.component';
import {LayoutModule} from '@angular/cdk/layout';
import {LoadingInterceptor} from './shared/services/loading.interceptor';
import {LoggerModule, NgxLoggerLevel} from 'ngx-logger';
import {MAT_SNACK_BAR_DEFAULT_OPTIONS, MatSnackBarModule} from '@angular/material/snack-bar';
import {MapComponent} from './map/map.component';
import {MarkdownModule} from 'ngx-markdown';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatButtonModule} from '@angular/material/button';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatCardModule} from '@angular/material/card';
import {MatChipsModule} from '@angular/material/chips';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatMenuModule} from '@angular/material/menu';
import {MatNativeDateModule} from '@angular/material/core';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSelectModule} from '@angular/material/select';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatSortModule} from '@angular/material/sort';
import {MatTableModule} from '@angular/material/table';
import {MatTabsModule} from '@angular/material/tabs';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatTreeModule} from '@angular/material/tree';
import {MetricsComponent} from './admin/metrics/metrics.component';
import {NgModule} from '@angular/core';
import {NgxMapboxGLModule} from 'ngx-mapbox-gl';
import {NoteDetailsComponent} from './notes/detail/note-details.component';
import {NotesComponent} from './notes/list/notes.component';
import {PlaceAddComponent} from './places/add/place-add.component';
import {PlaceDetailComponent} from './places/detail/place-detail.component';
import {PlaceEditComponent} from './places/edit/place-edit.component';
import {PlacesComponent} from './places/list/places.component';
import {UserProfileComponent} from './user-profile/user-profile.component';
import {WebStorageModule} from 'ngx-web-storage';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatExpansionModule} from '@angular/material/expansion';
import {TagInputComponent} from './shared/components/tag-input/tag-input.component';
import {UserDisplayComponent} from './shared/components/users/display/user-display.component';
import {NgxStarsModule} from 'ngx-stars';
import { UserSelectComponent } from './shared/components/users/select/user-select.component';

//  imports makes the exported declarations of other modules available in the current module
//  declarations are to make directives (including components and pipes) from the current module available to other
//    directives in the current module. Selectors of directives components or pipes are only matched against the HTML
//   if they are declared or imported.
//  providers are to make services and values known to DI (dependency injection). They are added to the root scope and
// they are injected to other services or directives that have them as dependency.
@NgModule({
  declarations: [
    AppComponent,
    AreaDisplayComponent,
    AreaTreeComponent,
    BytesizePipe,
    ConfirmDialogComponent,
    DishAddComponent,
    DishDetailComponent,
    DishEditComponent,
    DishesComponent,
    FileInputDialogComponent,
    FileUploadComponent,
    HomeComponent,
    MapComponent,
    MetricsComponent,
    NoteDetailsComponent,
    NotesComponent,
    PlaceAddComponent,
    PlaceDetailComponent,
    PlaceEditComponent,
    PlacesComponent,
    UserProfileComponent,
    TagInputComponent,
    UserDisplayComponent,
    UserSelectComponent
  ],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    DateFnsModule.forRoot(), // https://github.com/joanllenas/ngx-date-fns
    FormsModule,
    HttpClientModule,
    LayoutModule,
    LayoutModule,
    MarkdownModule.forRoot(),
    MatAutocompleteModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatChipsModule,
    MatExpansionModule,
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
    MatTabsModule,
    MatToolbarModule,
    LoggerModule.forRoot({
      // serverLoggingUrl: '/api/logs',
      level: NgxLoggerLevel.DEBUG,
      // serverLogLevel: NgxLoggerLevel.ERROR,
      disableConsoleLogging: false
    }),
    MatSelectModule,
    MatDatepickerModule,
    MatDialogModule,
    MatButtonToggleModule,
    ReactiveFormsModule,
    MatSlideToggleModule,
    NgxMapboxGLModule,
    NgxStarsModule,
    WebStorageModule.forRoot()
  ],
  exports: [],
  providers: [
    // intercept all http requests for progress (aka loading) bar
    {provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true},
    // default duration for snackbar messages
    // https://material.angular.io/components/snack-bar/overview#setting-the-global-configuration-defaults
    {provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: {
      duration: 2500, verticalPosition: 'top', horizontalPosition : 'right' // 'start' | 'center' | 'end' | 'left' | 'right';
    }}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
