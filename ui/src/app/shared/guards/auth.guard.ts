import { Injectable } from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router} from '@angular/router';
import { Observable } from 'rxjs';
import {WebStorageService} from 'ngx-web-storage';
import {NGXLogger} from 'ngx-logger';

export const PRE_LOGIN_URL_SESSION_KEY = 'preloginUrl';
@Injectable({
  providedIn: 'root'
})
/**
 * https://juristr.com/blog/2018/11/better-route-guard-redirects/
 * https://stackblitz.com/edit/angular-auth-guard-service?file=src%2Fapp%2Fauth.service.ts
 */
export class AuthGuard implements CanActivate {

  constructor(private storage: WebStorageService,
              private logger: NGXLogger,
              private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const previousPath = this.storage.session.get(PRE_LOGIN_URL_SESSION_KEY);
    if (previousPath) {
      this.logger.info(`${PRE_LOGIN_URL_SESSION_KEY} found in session, redirecting to ${previousPath}`);
      this.router.navigateByUrl(previousPath);
      return false;
    } else {
      this.logger.debug(`No previous url found, activating default login-successful route`);
      return true;
    }
  }

}
