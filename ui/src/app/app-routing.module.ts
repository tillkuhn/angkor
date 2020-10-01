import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PlacesComponent} from './places/places.component';
import {PlaceDetailComponent} from './place-detail/place-detail.component';
import {PlaceAddComponent} from './place-add/place-add.component';
import {PlaceEditComponent} from './place-edit/place-edit.component';
import {CommonModule} from '@angular/common';
import {MapComponent} from './map/map.component';
import {HomeComponent} from './home/home.component';
import {DishesComponent} from './dishes/dishes.component';
import {NotesComponent} from './notes/notes.component';
import {MetricsComponent} from './admin/metrics/metrics.component';
import {UserProfileComponent} from './user-profile/user-profile.component';
import {DishAddComponent} from './dishes/dish-add/dish-add.component';

const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent,
    data: {title: 'Home'}
  },
  {
    path: 'map',
    component: MapComponent,
    data: {title: 'Map'}
  },
  {
    path: 'dishes',
    component: DishesComponent,
    data: {title: 'Dishes'}
  },
  {
    path: 'dish-add',
    component: DishAddComponent,
    data: {title: 'Add Dish'}
  },
  {
    path: 'notes',
    component: NotesComponent,
    data: {title: 'Notes'}
  },
  {
    path: 'places',
    component: PlacesComponent,
    data: {title: 'List of Places'}
  },
  {
    path: 'place-details/:id',
    component: PlaceDetailComponent,
    data: {title: 'Place Details'}
  },
  {
    path: 'place-add',
    component: PlaceAddComponent,
    data: {title: 'Add Place'}
  },
  {
    path: 'place-edit/:id',
    component: PlaceEditComponent,
    data: {title: 'Edit Place'}
  },
  {
    path: 'my-profile',
    component: UserProfileComponent,
    data: {title: 'My Profile'}
  },
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {
    path: 'admin/metrics',
    component: MetricsComponent,
    data: {title: 'Admin Metrics'}
  },

];

@NgModule({
  imports: [CommonModule,
    RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
