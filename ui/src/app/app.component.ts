import {Component} from '@angular/core';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {Observable} from 'rxjs';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {map, shareReplay} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  constructor(private matIconRegistry: MatIconRegistry,
              private breakpointObserver: BreakpointObserver,
              private _snackBar: MatSnackBar,
              private domSanitizer: DomSanitizer) {
    // https://www.digitalocean.com/community/tutorials/angular-custom-svg-icons-angular-material
    this.matIconRegistry.addSvgIcon(
      `backpack`,
      this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/backpack.svg')
    );
  }

  title = 'TiMaFe on Air';

  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay()
    );

  openSnackBar(message: string, action: string) {
    this._snackBar.open(message, action, {
      duration: 2000,
    });
  }

}
