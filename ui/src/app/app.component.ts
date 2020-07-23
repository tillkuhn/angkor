import {Component, OnInit} from '@angular/core';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {Observable} from 'rxjs';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {map, shareReplay} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LoadingService} from './shared/loading.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {

  title = 'TiMaFe on Air';
  isLoading: boolean;

  constructor(private matIconRegistry: MatIconRegistry,
              private breakpointObserver: BreakpointObserver,
              private _snackBar: MatSnackBar, public loadingService: LoadingService,
              private domSanitizer: DomSanitizer) {
    // https://www.digitalocean.com/community/tutorials/angular-custom-svg-icons-angular-material
    this.matIconRegistry.addSvgIcon(
      `backpack`,
      this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/backpack.svg')
    );
  }

  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay()
    );

  ngOnInit() {
    this.loadingService.isLoading.subscribe(async data => {
      this.isLoading = await data;
    });
  }

  openSnackBar(message: string, action: string) {
    this._snackBar.open(message, action, {
      duration: 2000,
    });
  }

}
