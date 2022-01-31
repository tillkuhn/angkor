import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RatingComponent } from './rating.component';
import {MaterialModule} from '@shared/modules/material.module';

// How to create a feature Module: https://angular.io/guide/feature-modules
// ng g m shared/modules/Rating
@NgModule({
  declarations: [
    RatingComponent
  ],
  exports: [
    RatingComponent
  ],
  imports: [
    CommonModule,
    MaterialModule
  ]
})
export class RatingModule { }
