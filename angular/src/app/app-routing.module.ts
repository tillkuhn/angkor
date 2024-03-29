import {AreaTreeComponent} from './areas/area-tree.component';
import {CloudComponent} from '@app/clouds/cloud/cloud.component';
import {CommonModule} from '@angular/common';
import {DishAddComponent} from './dishes/add/dish-add.component';
import {DishDetailComponent} from './dishes/detail/dish-detail.component';
import {DishEditComponent} from './dishes/edit/dish-edit.component';
import {DishesComponent} from './dishes/list/dishes.component';
import {EntityMetadata, EntityType} from '@shared/domain/entities';
import {EventsComponent} from '@app/admin/events/events.component';
import {FeedComponent} from '@app/links/feeds/feed.component';
import {HildeGuard} from '@shared/guards/hilde.guard';
import {HomeComponent} from './home/home.component';
import {LocationSearchComponent} from '@app/locatables/search/location-search.component';
import {MapComponent} from './map/map.component';
import {MetricsComponent} from './admin/metrics/metrics.component';
import {MyProfileComponent} from './myprofile/my-profile.component';
import {NgModule} from '@angular/core';
import {NotesComponent} from './notes/list/notes.component';
import {PlaceAddComponent} from './places/add/place-add.component';
import {PlaceDetailComponent} from './places/detail/place-detail.component';
import {PlaceEditComponent} from './places/edit/place-edit.component';
import {RouterModule, Routes} from '@angular/router';
import {VideoPlayerComponent} from '@app/locatables/videos/video-player.component';
import {RadioComponent} from '@app/radio/radio.component';
import {RemoveMeComponent} from '@app/myprofile/remove-me.component';

const routes: Routes = [

  /* HomeZone */
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [HildeGuard], // may trigger redirect to pre-login url
    data: {title: 'HomeZone'}
  },
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {
    path: 'logout',
    component: HomeComponent,
    data: {title: 'Logout'}
  },

  /* Places Section */

  {
    path: 'places/add',
    component: PlaceAddComponent,
    data: {title: 'Add Place'}
  },
  {
    path: 'places/details/:id',
    component: PlaceDetailComponent,
    data: {title: 'Place Details'}
  },
  {
    path: 'places/edit/:id',
    component: PlaceEditComponent,
    data: {title: 'Edit Place'}
  },

  /* Dish Section */

  {
    path: EntityMetadata[EntityType.Dish].path,
    component: DishesComponent,
    data: {title: 'Dishes'}
  },
  {
    path: 'dishes/add',
    component: DishAddComponent,
    data: {title: 'Add Dish'}
  },
  {
    path: 'dishes/edit/:id',
    component: DishEditComponent,
    data: {title: 'Edit Dish'}
  },
  {
    path: 'dishes/details/:id',
    component: DishDetailComponent,
    data: {title: 'Dish Details'}
  },

  /* Notes Section */

  {
    path: 'notes',
    component: NotesComponent,
    data: {title: 'Notes'}
  },
  {
    path: 'notes/:id',
    component: NotesComponent,
    data: {title: 'Note Details'}
  },

  /* Links Section*/

  {
    path: 'feeds',
    component: FeedComponent,
    data: {title: 'TiMaFeeds'}
  },

  /* Tag Clouds */

  {
    path: 'clouds',
    component: CloudComponent,
    data: {title: 'Word Tag Clouds'}
  },

  /* New Unified Locations Search (Tours, Videos, Posts) */

  {
    path: 'places',
    component: LocationSearchComponent,
    data: {title: 'List of Places', entityType: EntityType.Place}
  },
  {
    path: 'tours',
    component: LocationSearchComponent,
    data: {title: 'World of Tours', entityType: EntityType.Tour}
  },
  {
    path: 'tours/:id',
    component: LocationSearchComponent,
    data: {title: 'Tours Details', entityType: EntityType.Tour}
  },
  {
    path: EntityMetadata[EntityType.Post].path,
    component: LocationSearchComponent,
    data: {title: 'Blog Posts', entityType: EntityType.Post}
  },
  {
    path: 'posts/:id',
    component: LocationSearchComponent,
    data: {title: 'Post Details', entityType: EntityType.Post}
  },
  {
    path: 'photos',
    component: LocationSearchComponent,
    data: {title: 'Blog Posts', entityType: EntityType.Photo}
  },
  {
    path: 'photos/:id',
    component: LocationSearchComponent,
    data: {title: 'Photo Details', entityType: EntityType.Photo}
  },
  {
    path: 'videos',
    component: LocationSearchComponent,
    data: {title: 'Schaumerma', entityType: EntityType.Video}
  },
  {
    path: 'videos/:id',
    component: LocationSearchComponent,
    data: {title: 'Schaumerma Details', entityType: EntityType.Video}
  },
  {
    path: 'songs',
    component: RadioComponent,
    data: {title: 'Play it again', entityType: EntityType.Song}
  },

  // Deeplink to play videos
  {
    path: 'player/:id',
    component: VideoPlayerComponent,
    data: {title: 'Play Video'}
  },

  /* Shared Section */
  {
    path: 'map',
    component: MapComponent,
    data: {title: 'Map'}
  },
  {
    path: 'map/:coordinates', // e.g. map/@13.7499533,100.4891229 for BKK @lat,lon
    component: MapComponent,
    data: {title: 'Map'}
  },
  {
    path: 'my-profile',
    component: MyProfileComponent,
    data: {title: 'My Profile'}
  },
  {
    path: 'remove-me',
    component: RemoveMeComponent,
    data: {title: 'Remove Me'}
  },
  {
    path: 'area-tree',
    component: AreaTreeComponent,
    data: {title: 'Area Tree'}
  },
  /* Admin Section */
  {
    path: 'admin/metrics',
    component: MetricsComponent,
    data: {title: 'Admin Metrics'}
  },
  {
    path: 'admin/events',
    component: EventsComponent,
    data: {title: 'Topic Events'}
  },
  /* Experimental */


];

@NgModule({
  imports: [CommonModule,
    RouterModule.forRoot(routes, {})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
