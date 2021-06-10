import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {map, shareReplay, takeUntil} from 'rxjs/operators';
import {LoadingService} from '@shared/services/loading.service';
import {MatSidenav} from '@angular/material/sidenav';
import {MatDrawerToggleResult} from '@angular/material/sidenav/drawer';
import {EnvironmentService} from '@shared/services/environment.service';
import {NGXLogger} from 'ngx-logger';
import {AuthService} from '@shared/services/auth.service';
import {NotificationService} from '@shared/services/notification.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {

  title = 'TiMaFe on Air';

  imprintUrl: string;
  isLoading: boolean;
  private ngUnsubscribe = new Subject();

  isHandset$: Observable<boolean> = this.breakpointObserver
    .observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay(),
      // https://ncjamieson.com/avoiding-takeuntil-leaks/ should be last in sequence
      takeUntil(this.ngUnsubscribe),
    );

  constructor(private breakpointObserver: BreakpointObserver,
              private notifier: NotificationService, // make sure it's init early
              public loadingService: LoadingService,
              public authService: AuthService,
              public envService: EnvironmentService,
              private logger: NGXLogger
  ) {
  }

  ngOnInit() {
    this.imprintUrl = this.envService.imprintUrl;
    this.loadingService.isLoading$
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe(async data => {
         this.isLoading = await data;
      });
  }

  // https://stackoverflow.com/questions/38008334/angular-rxjs-when-should-i-unsubscribe-from-subscription
  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  // Result of the toggle promise that indicates the state of the drawer.
  // export declare type MatDrawerToggleResult = 'open' | 'close';
  // https://angular.io/guide/observables-in-angular
  closeIfHandset(drawer: MatSidenav): Promise<MatDrawerToggleResult> {
    return new Promise<MatDrawerToggleResult>((resolve, _) => {
      this.isHandset$.subscribe(isHandset => {
        if (isHandset) {
          drawer.close().then(result => {
            if (result !== 'close') {
              this.logger.warn('unexpected return state ' + result + ' during close drawer');
            }
          });
          resolve('close');
        } else {
          this.logger.trace('desktop mode, keep open');
          resolve('open');
        }
      });
    });
  }

}
