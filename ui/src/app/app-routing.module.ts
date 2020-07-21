import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { PlacesComponent } from './places/places.component';
import { PlaceDetailComponent } from './place-detail/place-detail.component';
import { PlaceAddComponent } from './place-add/place-add.component';
import { PlaceEditComponent } from './place-edit/place-edit.component';
import {CommonModule} from '@angular/common';
import {MapComponent} from './map/map.component';
import {HomeComponent} from './home/home.component';
import {DishesComponent} from './dishes/dishes.component';

const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent,
    data: { title: 'Home' }
  },
  {
    path: 'map',
    component: MapComponent,
    data: { title: 'Map' }
  },
  {
    path: 'dishes',
    component: DishesComponent,
    data: { title: 'Dishes' }
  },
  {
    path: 'places',
    component: PlacesComponent,
    data: { title: 'List of Places' }
  },
  {
    path: 'place-details/:id',
    component: PlaceDetailComponent,
    data: { title: 'Place Details' }
  },
  {
    path: 'place-add',
    component: PlaceAddComponent,
    data: { title: 'Add Place' }
  },
  {
    path: 'place-edit/:id',
    component: PlaceEditComponent,
    data: { title: 'Edit Place' }
  },
  { path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  }

];

@NgModule({
  imports: [CommonModule,
    RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
