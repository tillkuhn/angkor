// App Root Module, see https://angular.io/guide/feature-modules#importing-a-feature-module
// import the feature module here, so you can add it to the imports array below

import {PostDetailsComponent} from './locatables/posts/post-details.component';
import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {AreaDisplayComponent} from '@shared/components/area-display/area-display.component';
import {AreaTreeComponent} from './areas/area-tree.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {BrowserModule} from '@angular/platform-browser';
import {BytesizePipe} from '@shared/pipes/bytesize.pipe';
import {CloudComponent} from './clouds/cloud/cloud.component';
import {ConfirmDialogComponent} from '@shared/components/confirm-dialog/confirm-dialog.component';
import {CoordinatesInputComponent} from '@shared/components/coordindates/input/coordinates-input.component';
import {DishAddComponent} from './dishes/add/dish-add.component';
import {DishDetailComponent} from './dishes/detail/dish-detail.component';
import {DishEditComponent} from './dishes/edit/dish-edit.component';
import {DishesComponent} from './dishes/list/dishes.component';
import {EventsComponent} from './admin/events/events.component';
import {FeedComponent} from './links/feeds/feed.component';
import {FileInputDialogComponent, FileUploadComponent} from '@shared/modules/imagine/file-upload/file-upload.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {HomeComponent} from './home/home.component';
import {HumanizeDatePipe} from '@shared/pipes/humanize-date.pipe';
import {IconModule} from '@shared/modules/icon.module';
import {LayoutModule} from '@angular/cdk/layout';
import {LoadingInterceptor} from '@shared/services/loading.interceptor';
import {LocationDetailsComponent} from '@app/locatables/location-details.component';
import {LocationSearchComponent} from './locatables/search/location-search.component';
import {LoggerModule, NgxLoggerLevel} from 'ngx-logger';
import {MAT_LEGACY_SNACK_BAR_DEFAULT_OPTIONS as MAT_SNACK_BAR_DEFAULT_OPTIONS} from '@angular/material/legacy-snack-bar';
import {MapComponent} from './map/map.component';
import {MarkdownModule} from 'ngx-markdown';
import {MaterialModule} from '@shared/modules/material.module'; // see separate module in shared/modules
import {MetricsComponent} from './admin/metrics/metrics.component';
import {MetricsModule} from '@shared/modules/metrics/metrics.module';
import {MyProfileComponent} from './myprofile/my-profile.component';
import {NgModule} from '@angular/core';
import {NgxMapboxGLModule} from 'ngx-mapbox-gl';
import {NoteDetailsComponent} from './notes/detail/note-details.component';
import {NotesComponent} from './notes/list/notes.component';
import {PhotoDetailsComponent} from '@app/locatables/photos/photo-details.component';
import {PlaceAddComponent} from './places/add/place-add.component';
import {PlaceDetailComponent} from './places/detail/place-detail.component';
import {PlaceEditComponent} from './places/edit/place-edit.component';
import {TagCloudComponent} from 'angular-tag-cloud-module';
import {TagInputComponent} from '@shared/components/tag-input/tag-input.component';
import {TourDetailsComponent} from './locatables/tours/tour-details.component';
import {UserDisplayComponent} from '@shared/components/users/display/user-display.component';
import {UserSelectComponent} from '@shared/components/users/select/user-select.component';
import {VideoDetailsComponent} from './locatables/videos/video-details.component';
import {VideoPlayerComponent} from './locatables/videos/video-player.component';
// import {LocalStorageService} from 'ngx-webstorage'; // see successor below
import {NgxWebstorageModule} from 'ngx-webstorage';
import {YouTubePlayerModule} from '@angular/youtube-player';
import {RadioComponent} from './radio/radio.component';
import {MatLegacySliderModule as MatSliderModule} from '@angular/material/legacy-slider';
import {RatingModule} from '@shared/modules/rating/rating.module';
import { RemoveMeComponent } from './myprofile/remove-me.component';
import { MAT_DATE_LOCALE } from '@angular/material/core';

@NgModule({
  // "declarations:" make directives (including components and pipes) from the *current* module available to
  // *other* directives in the current module. Selectors of directives components or pipes are only matched against
  // the HTML if they are declared or imported.
  declarations: [
    AppComponent,
    AreaDisplayComponent,
    AreaTreeComponent,
    BytesizePipe,
    CloudComponent,
    ConfirmDialogComponent,
    CoordinatesInputComponent,
    DishAddComponent,
    DishDetailComponent,
    DishEditComponent,
    DishesComponent,
    EventsComponent,
    FeedComponent,
    FileInputDialogComponent,
    FileUploadComponent,
    HomeComponent,
    HumanizeDatePipe,
    LocationDetailsComponent,
    LocationSearchComponent,
    MapComponent,
    MetricsComponent,
    MyProfileComponent,
    NoteDetailsComponent,
    NotesComponent,
    PhotoDetailsComponent,
    PlaceAddComponent,
    PlaceDetailComponent,
    PlaceEditComponent,
    PostDetailsComponent,
    RadioComponent,
    TagInputComponent,
    TourDetailsComponent,
    UserDisplayComponent,
    UserSelectComponent,
    VideoDetailsComponent,
    VideoPlayerComponent,
    RemoveMeComponent,
  ],

  // "imports:" makes the *exported* declarations of *other* modules available in the *current* module
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    HttpClientModule,
    IconModule, // Our Icon Module in shared/modules
    LayoutModule,
    LayoutModule,
    LoggerModule.forRoot({
      level: NgxLoggerLevel.DEBUG, // NgxLoggerLevels are: TRACE|DEBUG|INFO|LOG|WARN|ERROR|FATAL|OFF where DEBUG=1
      disableConsoleLogging: false
    }),
    MarkdownModule.forRoot(),
    MaterialModule, // here you'll find all the Material stuff
    MetricsModule,
    NgxMapboxGLModule,
    NgxWebstorageModule.forRoot(),
    RatingModule, // Our Rating Module in shared/modules
    ReactiveFormsModule,
    TagCloudComponent, // instead of TagCloudModule, see https://github.com/d-koppenhagen/angular-tag-cloud-module/releases/tag/14.0.0
    YouTubePlayerModule,
    MatSliderModule,
    RatingModule
  ],

  // "exports:" is the set of components, directives, and pipes declared in this NgModule that can be used in the template of any component that
  // is part of an NgModule that imports this NgModule. Exported declarations are the module's public API.
  exports: [],

  // "providers:" are to make services and values known to DI (dependency injection). They are added to the root scope, and
  // they are injected to other services or directives that have them as dependency.
  providers: [
    {
      // intercept all http requests for progress (aka loading) bar
      provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true
    },
    {
      // override the default snack bar options using the MAT_SNACK_BAR_DEFAULT_OPTIONS injection token.
      // https://material.angular.io/components/snack-bar/overview#setting-the-global-configuration-defaults
      provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: {
        duration: 2500, verticalPosition: 'top', horizontalPosition: 'right' // 'start' | 'center' | 'end' | 'left' | 'right';
      }
    },
    // overwrite for date format, so we have dd.mm.yyyy instead of mm/dd/yyyy
    // https://material.angular.io/components/datepicker/overview#setting-the-locale-code
    {provide: MAT_DATE_LOCALE, useValue: 'de-DE'}
  ],
  // "bootstrap:" is the set of components that are bootstrapped when this module is bootstrapped.
  // The components listed here are automatically added to entryComponents.
  bootstrap: [AppComponent]
})
export class AppModule {
  // deliberately empty
}
