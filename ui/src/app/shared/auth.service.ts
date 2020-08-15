import { Injectable } from '@angular/core';
import { Location } from '@angular/common';
// import {EnvironmentService} from "../environment.service";
import {environment} from '../../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {BehaviorSubject, Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import { share} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {User} from '../domain/user';
// import { AuthServerProvider } from 'app/core/auth/auth-session.service';

/**
 * https://netbasal.com/angular-2-persist-your-login-status-with-behaviorsubject-45da9ec43243
 * Persisting user authentication with BehaviorSubject in Angular
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(private http: HttpClient, private logger: NGXLogger, private location: Location ) {
    this.checkAuthenticated();
  }

  public currentUser: User;
  //  use .share() when creating the isLoginSubject Observable, so async pipes don’t create multiple subscriptions.
  isAuthenticatedSubject = new BehaviorSubject<boolean>(false);

  // A subject in Rx is both Observable and Observer. In this case, we only care about the Observable part,
  // letting other parts of our app the ability to subscribe to our Observable.
  isAuthenticated(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable().pipe(share());;
  }

  login() {
    // If you have configured multiple OIDC providers, then, you can update this URL to /login.
    // It will show a Spring Security generated login page with links to configured OIDC providers.
    // location.href = `${location.origin}${this.location.prepareExternalUrl('oauth2/authorization/oidc')}`;
    this.logger.debug(location.origin,this.location.prepareExternalUrl('oauth2/authorization/cognito'));
    // location.href = `${location.origin}${this.location.prepareExternalUrl('oauth2/authorization/cognito')}`;
    location.href = `${environment.apiUrlRoot}/../..${this.location.prepareExternalUrl('oauth2/authorization/cognito')}`;
  }

  checkAuthenticated() {
    this.http.get<any>(environment.apiUrlRoot + '/authenticated')
      .subscribe(res => {
        this.logger.info('check auth='+res);
        this.setAuthenticated(res);
        if (res) {
          this.http.get<User>(environment.apiUrlRoot + '/account').subscribe( user => this.currentUser = user);
        }
      });
  }

  setAuthenticated(state:boolean) {
    this.isAuthenticatedSubject.next(state);
  }

  logout() {
    this.logger.warn('logout user ');
    this.setAuthenticated(false);
    this.http.post(environment.apiUrlRoot + '/logout', {}, { observe: 'response' }).subscribe(
      response => {
          const data = response.body;
          this.logger.info(`todo call logoutUrl`);
      }
      // map((response: HttpResponse<any>) => {
        // to get a new csrf token call the api
        // this.http.get(SERVER_API_URL + 'api/account').subscribe(() => {}, () => {});
        // return response;
      // })
    );
    /*
    logout(): Observable<any> {
      // logout from the server
      return this.http.post(SERVER_API_URL + 'api/logout', {}, { observe: 'response' }).pipe(
        map((response: HttpResponse<any>) => {
          // to get a new csrf token call the api
          this.http.get(SERVER_API_URL + 'api/account').subscribe(() => {}, () => {});
          return response;
        })
      );
    }
    */
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
