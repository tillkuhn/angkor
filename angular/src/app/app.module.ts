import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {AreaDisplayComponent} from '@shared/components/area-display/area-display.component';
import {AreaTreeComponent} from './areas/area-tree.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {BrowserModule} from '@angular/platform-browser';
import {BytesizePipe} from '@shared/pipes/bytesize.pipe';
import {ConfirmDialogComponent} from '@shared/components/confirm-dialog/confirm-dialog.component';
import {DateFnsModule} from 'ngx-date-fns';
import {DishAddComponent} from './dishes/add/dish-add.component';
import {DishDetailComponent} from './dishes/detail/dish-detail.component';
import {DishEditComponent} from './dishes/edit/dish-edit.component';
import {DishesComponent} from './dishes/list/dishes.component';
import {FileInputDialogComponent, FileUploadComponent} from '@shared/modules/imagine/file-upload/file-upload.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {HomeComponent} from './home/home.component';
import {IconModule} from '@shared/modules/icon.module';
import {LayoutModule} from '@angular/cdk/layout';
import {LoadingInterceptor} from '@shared/services/loading.interceptor';
import {LoggerModule, NgxLoggerLevel} from 'ngx-logger';
import {MAT_SNACK_BAR_DEFAULT_OPTIONS} from '@angular/material/snack-bar';
import {MapComponent} from './map/map.component';
import {MarkdownModule} from 'ngx-markdown';
import {MaterialModule} from '@shared/modules/material.module'; // see separate module in shared/modules
import {MetricsComponent} from './admin/metrics/metrics.component';
import {MyProfileComponent} from './myprofile/my-profile.component';
import {NgModule} from '@angular/core';
import {NgxMapboxGLModule} from 'ngx-mapbox-gl';
import {NgxStarsModule} from 'ngx-stars';
import {NoteDetailsComponent} from './notes/detail/note-details.component';
import {NotesComponent} from './notes/list/notes.component';
import {PlaceAddComponent} from './places/add/place-add.component';
import {PlaceDetailComponent} from './places/detail/place-detail.component';
import {PlaceEditComponent} from './places/edit/place-edit.component';
import {PlacesComponent} from './places/list/places.component';
import {TagInputComponent} from '@shared/components/tag-input/tag-input.component';
import {UserDisplayComponent} from '@shared/components/users/display/user-display.component';
import {UserSelectComponent} from '@shared/components/users/select/user-select.component';
import {WebStorageModule} from 'ngx-web-storage';
import {YouTubePlayerModule} from '@angular/youtube-player';

// App Modules /see https://angular.io/guide/feature-modules#importing-a-feature-module)
// import the feature module here so you can add it to the imports array below
import {MetricsModule} from '@shared/modules/metrics/metrics.module';
import {VideoComponent} from './links/videos/video.component';
import {LinkDetailsComponent} from './links/detail/link-details.component';
import {CoordinatesInputComponent} from '@shared/components/coordindates/input/coordinates-input.component';
import {FeedComponent} from './links/feeds/feed.component';
import {TagCloudModule} from 'angular-tag-cloud-module';
import {CloudComponent} from './clouds/cloud/cloud.component';
import {EventsComponent} from './admin/events/events.component';
import {LocationListComponent} from './locations/locations/location-list.component';
import {TourDetailsComponent} from './locations/tours/tour-details.component';

@NgModule({
  // declarations:
  // are to make directives (including components and pipes) from the current module available to other
  // directives in the current module. Selectors of directives components or pipes are only matched against
  // the HTML if they are declared or imported.
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
    LinkDetailsComponent,
    MapComponent,
    MetricsComponent,
    NoteDetailsComponent,
    NotesComponent,
    PlaceAddComponent,
    PlaceDetailComponent,
    PlaceEditComponent,
    PlacesComponent,
    MyProfileComponent,
    TagInputComponent,
    UserDisplayComponent,
    UserSelectComponent,
    VideoComponent,
    CoordinatesInputComponent,
    FeedComponent,
    CloudComponent,
    EventsComponent,
    LocationListComponent,
    TourDetailsComponent,
  ],

  // imports: makes the exported declarations of other modules available in the current module
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    DateFnsModule.forRoot(), // https://github.com/joanllenas/ngx-date-fns
    FormsModule,
    HttpClientModule,
    IconModule, // Out Icon Module in shared/modules
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
    NgxStarsModule,
    ReactiveFormsModule,
    TagCloudModule,
    WebStorageModule.forRoot(),
    YouTubePlayerModule
  ],

  // exports: The set of components, directives, and pipes declared in this NgModule that can be used in the template of any component that
  //          is part of an NgModule that imports this NgModule. Exported declarations are the module's public API.
  exports: [],

  // providers: are to make services and values known to DI (dependency injection). They are added to the root scope and
  //            they are injected to other services or directives that have them as dependency.
  providers: [
    {
      // intercept all http requests for progress (aka loading) bar
      provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true
    },
    {
      // https://material.angular.io/components/snack-bar/overview#setting-the-global-configuration-defaults
      provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: {
        duration: 2500, verticalPosition: 'top', horizontalPosition: 'right' // 'start' | 'center' | 'end' | 'left' | 'right';
      }
    }
  ],
  // bootstrap: The set of components that are bootstrapped when this module is bootstrapped.
  //            The components listed here are automatically added to entryComponents.
  bootstrap: [AppComponent]
})
export class AppModule {
}
