import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CountUpComponent} from './count-up/count-up.component';

@NgModule({
  declarations: [CountUpComponent],
  exports: [
    CountUpComponent
  ],
  imports: [
    CommonModule
  ]
})
export class MetricsModule { }
