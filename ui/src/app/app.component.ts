import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subscription} from 'rxjs';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {map, shareReplay} from 'rxjs/operators';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LoadingService} from './shared/services/loading.service';
import {MatSidenav} from '@angular/material/sidenav';
import {MatDrawerToggleResult} from '@angular/material/sidenav/drawer';
import {EnvironmentService} from './shared/services/environment.service';
import {NGXLogger} from 'ngx-logger';
import {AuthService} from './shared/services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {

  title = 'TiMaFe on Air';

  imprintUrl: string;
  isLoading: boolean;
  isLoadingSubscription$: Subscription; // so we can unsubscribe onDestroy

  isHandset$: Observable<boolean> = this.breakpointObserver
    .observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay()
    );

  constructor(private breakpointObserver: BreakpointObserver,
              private snackBar: MatSnackBar,
              public loadingService: LoadingService,
              public authService: AuthService,
              public envService: EnvironmentService,
              private logger: NGXLogger
  ) {
  }

  ngOnInit() {
    this.imprintUrl = this.envService.imprintUrl;
    this.isLoadingSubscription$ = this.loadingService.isLoading$.subscribe(async data => {
      this.isLoading = await data;
    });
  }

  ngOnDestroy(): void {
    // https://blog.bitsrc.io/6-ways-to-unsubscribe-from-observables-in-angular-ab912819a78f
    if (this.isLoadingSubscription$) {
      this.isLoadingSubscription$.unsubscribe();
    }
  }

  // Result of the toggle promise that indicates the state of the drawer.
  // export declare type MatDrawerToggleResult = 'open' | 'close';
  // https://angular.io/guide/observables-in-angular
  closeIfHandset(drawer: MatSidenav): Promise<MatDrawerToggleResult> {
    return new Promise<MatDrawerToggleResult>((resolve, reject) => {
      this.isHandset$.subscribe(isHandset => {
        if (isHandset) {
          drawer.close().then(result => {
            if (result !== 'close') {
              this.logger.warn('unexpected return state ' + result + ' during close drawer');
            }
          });
          resolve('close');
        } else {
          this.logger.trace('deskop mode, keep open');
          resolve('open');
        }
      });
    });
  }

}
