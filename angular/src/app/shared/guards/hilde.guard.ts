import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
// import {WebStorageService} from 'ngx-web-storage';
import {LocalStorageService} from 'ngx-webstorage';
import {NGXLogger} from 'ngx-logger';

export const PRE_LOGIN_URL_SESSION_KEY = 'preloginUrl';

@Injectable({
  providedIn: 'root'
})
/**
 * https://juristr.com/blog/2018/11/better-route-guard-redirects/
 * https://stackblitz.com/edit/angular-auth-guard-service?file=src%2Fapp%2Fauth.service.ts
 */
export class HildeGuard implements CanActivate {

  private readonly className = 'HildeGuard';

  constructor(private storage: LocalStorageService,
              private logger: NGXLogger,
              private router: Router) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const previousPath = this.storage.retrieve(PRE_LOGIN_URL_SESSION_KEY);
    if (previousPath) {
      this.logger.info(`${this.className}.canActivate: ${PRE_LOGIN_URL_SESSION_KEY} found in session, redirecting to ${previousPath}`);
      this.storage.clear(PRE_LOGIN_URL_SESSION_KEY); // clean for next home access
      this.router.navigateByUrl(previousPath);
      return false;
    } else {
      this.logger.debug(`${this.className}.canActivate: No stored url found, activating default login-successful route`);
      return true;
    }
  }

}
