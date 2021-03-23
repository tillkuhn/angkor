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
  private iconPath = '../../../assets/icons';

  constructor(
    private domSanitizer: DomSanitizer,
    public matIconRegistry: MatIconRegistry
  ) {
    this.trustIcon('home', `${this.iconPath}/backpack.svg`);
    this.trustIcon('bowl', `${this.iconPath}/bowl.svg`);
    this.trustIcon('video', `${this.iconPath}/camera.svg`);
    this.trustIcon('dish', `${this.iconPath}/bowl.svg`); // there's also a single chili and 2 chilis
    this.trustIcon('place', `${this.iconPath}/island.svg`);
    this.trustIcon('note', `${this.iconPath}/notebook.svg`);
    this.trustIcon('world', `${this.iconPath}/world.svg`);
  }

  private trustIcon(iconName: string, resourceUrl: string): void {
    this.matIconRegistry.addSvgIcon(
      iconName, this.domSanitizer.bypassSecurityTrustResourceUrl(resourceUrl)
    );
  }
}
