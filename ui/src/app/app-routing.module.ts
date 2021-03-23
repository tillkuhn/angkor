import {AreaTreeComponent} from './areas/area-tree.component';
import {CommonModule} from '@angular/common';
import {DishAddComponent} from './dishes/add/dish-add.component';
import {DishDetailComponent} from './dishes/detail/dish-detail.component';
import {DishEditComponent} from './dishes/edit/dish-edit.component';
import {DishesComponent} from './dishes/list/dishes.component';
import {HomeComponent} from './home/home.component';
import {MapComponent} from './map/map.component';
import {MetricsComponent} from './admin/metrics/metrics.component';
import {NgModule} from '@angular/core';
import {NotesComponent} from './notes/list/notes.component';
import {PlaceAddComponent} from './places/add/place-add.component';
import {PlaceDetailComponent} from './places/detail/place-detail.component';
import {PlaceEditComponent} from './places/edit/place-edit.component';
import {PlacesComponent} from './places/list/places.component';
import {RouterModule, Routes} from '@angular/router';
import {MyProfileComponent} from './myprofile/my-profile.component';
import {HildeGuard} from '@shared/guards/hilde.guard';
import {VideoComponent} from './player/video.component';

const routes: Routes = [

  /* HomeZone */
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [HildeGuard], // may trigger redirect to prelogin url
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
    path: 'places',
    component: PlacesComponent,
    data: {title: 'List of Places'}
  },
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
    path: 'dishes',
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
    path: 'tube',
    component: VideoComponent,
    data: {title: 'TiMaFe Tube'}
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

];

@NgModule({
  imports: [CommonModule,
    RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
