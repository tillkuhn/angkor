// Third Example - icon module
import {NgModule} from '@angular/core';
import {DomSanitizer} from '@angular/platform-browser';
import {MatIconRegistry} from '@angular/material/icon';
import {MaterialModule} from './material.module';

/**
 * Inspired by https://www.educative.io/edpresso/angular-material-icon-component and
 * https://github.com/Anglebrackets-io/mat-icon-demo/blob/master/mat-icon-demo/apps/mat-icon-demo/src/app/shared/icon.module.ts
 *
 * You anywhere like this: <mat-icon svgIcon="camera"></mat-icon>
 */
@NgModule({
  declarations: [],
  imports: [MaterialModule],
  exports: [],
  providers: []
})
export class IconModule {
  private path = '../../../assets';

  constructor(
    private domSanitizer: DomSanitizer,
    public matIconRegistry: MatIconRegistry
  ) {
    this.trustIcon('place', `${this.path}/backpack.svg`);
    this.trustIcon('bowl', `${this.path}/bowl.svg`);
    this.trustIcon('dish', `${this.path}/chilis.svg`);
    this.trustIcon('note', `${this.path}/notebook.svg`);
    this.trustIcon('world', `${this.path}/world.svg`);
    this.trustIcon('video', `${this.path}/camera.svg`);
  }

  private trustIcon(iconName: string, resourceUrl: string): void {
    this.matIconRegistry.addSvgIcon(
      iconName, this.domSanitizer.bypassSecurityTrustResourceUrl(resourceUrl)
    );
  }
}
