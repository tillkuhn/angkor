import {AuthService} from '@shared/services/auth.service';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {EnvironmentService} from '@shared/services/environment.service';
import {LoadingService} from '@shared/services/loading.service';
import {MatDrawerToggleResult} from '@angular/material/sidenav/drawer';
import {MatSidenav} from '@angular/material/sidenav';
import {NGXLogger} from 'ngx-logger';
import {NotificationService} from '@shared/services/notification.service';
import {Observable} from 'rxjs';
import {WithDestroy} from '@shared/mixins/with-destroy';
import {map, shareReplay, takeUntil} from 'rxjs/operators';
import {EntityMetadata} from '@shared/domain/entities';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent extends WithDestroy() implements OnInit, OnDestroy {

  readonly title = 'TiMaFe on Air';
  readonly entityMetaData = EntityMetadata;

  imprintUrl: string;
  isLoading: boolean;

  // emit new results for any changes in matching of the given media queries (useful for conditional styles)
  isHandset$: Observable<boolean> = this.breakpointObserver
    .observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay(),
      // https://ncjamieson.com/avoiding-takeuntil-leaks/ should be last in sequence
      takeUntil(this.destroy$),
    );

  constructor(private breakpointObserver: BreakpointObserver, // Utility for checking the matching state of @media queries.
              private notifier: NotificationService, // make sure it's init early
              public loadingService: LoadingService,
              public authService: AuthService,
              public envService: EnvironmentService,
              private logger: NGXLogger
  ) {
    super();
  }

  ngOnInit() {
    this.imprintUrl = this.envService.imprintUrl;
    this.loadingService.isLoading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(async data => {
        this.isLoading = await data; // DO NOT remove await here despite the "Redundant Await" warning - it will cause errors
      });
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
