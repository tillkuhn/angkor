import { Injectable } from '@angular/core';
import { Location } from '@angular/common';
// import {EnvironmentService} from "../environment.service";
import {environment} from '../../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {BehaviorSubject, Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {User} from '../domain/user';
// import { AuthServerProvider } from 'app/core/auth/auth-session.service';

@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(private http: HttpClient,
              private logger: NGXLogger, private location: Location /*, private authServerProvider: AuthServerProvider*/) {}

  public isAuthenticated = new BehaviorSubject(false);
  public currentUser: User;

  login() {
    // If you have configured multiple OIDC providers, then, you can update this URL to /login.
    // It will show a Spring Security generated login page with links to configured OIDC providers.
    // location.href = `${location.origin}${this.location.prepareExternalUrl('oauth2/authorization/oidc')}`;
    this.logger.debug(location.origin,this.location.prepareExternalUrl('oauth2/authorization/cognito'));
    // location.href = `${location.origin}${this.location.prepareExternalUrl('oauth2/authorization/cognito')}`;
    location.href = `${environment.apiUrlRoot}/../..${this.location.prepareExternalUrl('oauth2/authorization/cognito')}`;
  }
  getAccount(): Observable<User> {
    return this.http.get<User>(environment.apiUrlRoot + '/account')
      .pipe(
        tap(account => {
          this.currentUser = account;
          this.logger.debug('apiService fetched account' + account)
        })
      );
  }

  checkAuthenticated() {
    this.http.get<any>(environment.apiUrlRoot + '/authenticated')
      .subscribe(res => {
        this.logger.info('check auth='+res);
        this.setAuthenticated(res);
      });
  }

  setAuthenticated(state:boolean) {
    this.isAuthenticated.next(state);
  }

  logout() {
    this.logger.warn('logout to be implemented');
  //     this.authServerProvider.logout().subscribe(response => {
  //       const data = response.body;
  //       let logoutUrl = data.logoutUrl;
  //       const redirectUri = `${location.origin}${this.location.prepareExternalUrl('/')}`;
  //
  //       // if Keycloak, uri has protocol/openid-connect/token
  //       if (logoutUrl.indexOf('/protocol') > -1) {
  //         logoutUrl = logoutUrl + '?redirect_uri=' + redirectUri;
  //       } else {
  //         // Okta
  //         logoutUrl = logoutUrl + '?id_token_hint=' + data.idToken + '&post_logout_redirect_uri=' + redirectUri;
  //       }
  //       window.location.href = logoutUrl;
  //     });
  //
  }

}
