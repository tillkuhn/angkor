import { Component } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  constructor(private matIconRegistry: MatIconRegistry,private domSanitizer: DomSanitizer){
    // https://www.digitalocean.com/community/tutorials/angular-custom-svg-icons-angular-material
    this.matIconRegistry.addSvgIcon(
      `backpack`,
      this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/backpack.svg')
    );
  }
  title = 'angkor-crud';
}
