import {Injectable} from '@angular/core';
import {Location} from '@angular/common';
import {environment} from '../../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {BehaviorSubject, Observable} from 'rxjs';
import {share} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {User, UserSummary} from '../domain/user';
import {WebStorageService} from 'ngx-web-storage';
import {PRE_LOGIN_URL_SESSION_KEY} from './guards/hilde.guard';
import {Router} from '@angular/router';

// import { AuthServerProvider } from 'app/core/auth/auth-session.service';

declare type AuthRole = 'ROLE_USER' | 'ROLE_ADMIN';

/**
 * Persisting user authentication with BehaviorSubject in Angular
 * https://netbasal.com/angular-2-persist-your-login-status-with-behaviorsubject-45da9ec43243
 */
@Injectable({providedIn: 'root'})
export class AuthService {

  currentUserSubject = new BehaviorSubject<User>(null);
  isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  private userSummaryLookup: Map<string, UserSummary> = new Map();

  // web store: https://stackblitz.com/edit/ngx-web-storage?file=app%2Fapp.component.ts
  constructor(
    private http: HttpClient,
    private logger: NGXLogger,
    private location: Location,
    private router: Router,
    private storage: WebStorageService) {
    this.checkAuthenticated(); // rest call to API
  }

  /**
   * Asks the backend via rest if current user is authenticated (!= anonymous), returns boolean resp.
   * if true, also loads details of current user (/account)
   */
  checkAuthenticated() {
    this.http.get<any>(environment.apiUrlRoot + '/authenticated')
      .subscribe(data => {
        this.logger.debug(`AuthService.checkAuthenticated() ${JSON.stringify(data)}`); // returns result=true or false
        this.isAuthenticatedSubject.next(data.result);
        if (data.result) { // means yes - we are authenticated
          this.http.get<User>(`${environment.apiUrlRoot}/account`).subscribe(
            user => {
              this.logger.debug(`checkAuthenticated() userId=${user.id}`);
              this.currentUserSubject.next(user);
            }
          );
          this.http.get<UserSummary[]>(`${environment.apiUrlRoot}/user-summaries`).subscribe(
            users => {
              this.logger.debug(`loaded ${users.length} user summaries`);
              this.userSummaryLookup.clear();
              users.forEach(userSummary => this.userSummaryLookup.set(userSummary.id, userSummary));
            }
          );
        } // end auth result == true block
      });
  }

  lookupUserSummary(userId: string): UserSummary {
    const us =  this.userSummaryLookup.get(userId);
    return us ? us : {id: '', shortname: 'undisclosed', emoji: '👤'};
  }

  // A subject in Rx is both Observable and Observer. In this case, we only care about the Observable part,

  // letting other parts of our app the ability to subscribe to our Observable.
  get isAuthenticated$(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable().pipe(share());
  }

  get currentUser$(): Observable<User> {
    return this.currentUserSubject.asObservable().pipe(share());
  }

  // ... and the sync versions, returns last value of the subject
  get isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  get currentUser(): User {
    return this.currentUserSubject.value;
  }

  // Sync Role checkers ...
  get canEdit(): boolean {
    return this.hasRole('ROLE_USER');
  }

  // Role checkers ...
  get canDelete(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }

  get isAdmin(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }

  private hasRole(role: AuthRole) {
    return this.currentUserSubject.value
      && this.currentUserSubject.value.roles
      && (this.currentUserSubject.value.roles.indexOf(role) !== -1);
  }

  /**
   *  Called vy login button, triggers the OIDC login process
   */
  login() {
    // If you have configured multiple OIDC providers, then, you can update this URL to /login.
    // It will show a Spring Security generated login page with links to configured OIDC providers.
    const currentPath = this.location.path();
    this.logger.debug(`Storing currentPath in session $PRE_LOGIN_URL_SESSION_KEY $currentPath`);
    this.storage.session.set(PRE_LOGIN_URL_SESSION_KEY, currentPath); // router.routerState.snapshot.url
    this.logger.debug(location.origin, this.location.prepareExternalUrl('oauth2/authorization/cognito'));
    // location.href = `${location.origin}${this.location.prepareExternalUrl('oauth2/authorization/cognito')}`;
    location.href = `${environment.apiUrlRoot}/../..${this.location.prepareExternalUrl('oauth2/authorization/cognito')}`;
  }

  /**
   * Called by logout button  <button (click)="authService.logout()" ....
   */
  logout() {
    this.logger.warn('logout user ');
    this.isAuthenticatedSubject.next(false);
    this.currentUserSubject.next(null);
    this.storage.session.remove(PRE_LOGIN_URL_SESSION_KEY); // used for redirect after login
    this.http.post(`${environment.apiUrlRoot}/logout`, {}, {observe: 'response'}).subscribe(
      response => {
        // const data = response.body; // returns logoutUrl null and idToken
        this.logger.info(`${environment.apiUrlRoot}/logout returned`);
        this.router.navigate(['/logout']);
      }
      // map((response: HttpResponse<any>) => {
      // to get a new csrf token call the api
      // this.http.get(SERVER_API_URL + 'api/account').subscribe(() => {}, () => {});
      // return response;
      // })
    );

  }


}
