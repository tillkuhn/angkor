import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { PlacesComponent } from './places/places.component';
import { PlaceDetailComponent } from './place-detail/place-detail.component';
import { PlaceAddComponent } from './place-add/place-add.component';
import { PlaceEditComponent } from './place-edit/place-edit.component';

const routes: Routes = [
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
    redirectTo: '/places',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
