import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Location} from '@angular/common';
import {NGXLogger} from 'ngx-logger';
import {PRE_LOGIN_URL_SESSION_KEY} from '../guards/hilde.guard';
import {Router} from '@angular/router';
import {User, UserSummary} from '../../domain/user';
import {WebStorageService} from 'ngx-web-storage';
import {environment} from '../../../environments/environment';
import {share} from 'rxjs/operators';

// import { AuthServerProvider } from 'app/core/auth/auth-session.service';

declare type AuthRole = 'ROLE_USER' | 'ROLE_ADMIN';

/**
 * Persisting user authentication with BehaviorSubject in Angular
 * https://netbasal.com/angular-2-persist-your-login-status-with-behaviorsubject-45da9ec43243
 */
@Injectable({providedIn: 'root'})
export class AuthService {

  private readonly className = 'AuthService';
  private userSummaryLookup: Map<string, UserSummary> = new Map();

  private currentUserSubject = new BehaviorSubject<User>(null);
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);

  // web store: https://stackblitz.com/edit/ngx-web-storage?file=app%2Fapp.component.ts
  constructor(
    private http: HttpClient,
    private logger: NGXLogger,
    private location: Location,
    private router: Router,
    private storage: WebStorageService) {
    this.checkAuthenticated(); // check if authenticated, and if so - load the user
  }

  // A subject in Rx is both Observable and Observer. In this case, we only care about the Observable part,
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

  /**
   * Asks the backend via rest if current user is authenticated (!= anonymous), returns boolean resp.
   * if true, also loads details of current user (/account)
   */
  checkAuthenticated() {
    const operation = `${this.className}.checkAuthenticated`;
    this.http.get<any>(environment.apiUrlRoot + '/authenticated')
      .subscribe(data => {
        this.logger.debug(`${operation} ${JSON.stringify(data)}`); // returns result=true or false
        this.isAuthenticatedSubject.next(data.result);
        if (data.result) { // means yes - we are authenticated
          this.http.get<User>(`${environment.apiUrlRoot}/account`).subscribe(
            user => {
              this.logger.debug(`${operation} userId=${user.id}`);
              this.currentUserSubject.next(user);
            }
          );
          // authenticated users are also allowed to see summaries
          this.http.get<UserSummary[]>(`${environment.apiUrlRoot}/user-summaries`).subscribe(
            users => {
              this.logger.debug(`${operation} fetched ${users.length} user summaries`);
              this.userSummaryLookup.clear();
              users.forEach(userSummary => this.userSummaryLookup.set(userSummary.id, userSummary));
            }
          );
        } // end auth result == true block
      });
  }

  userSummaries(): UserSummary[] {
    const us: UserSummary[] = [];
    this.userSummaryLookup.forEach((value: UserSummary, key: string) => {
      us.push(value);
    });
    return us;
  }

  /**
   * Returns the summary by id, or undisclosed if id is not part of the map
   * @param userId
   */
  lookupUserSummary(userId: string): UserSummary {
    const us = this.userSummaryLookup.get(userId);
    return us ? us : {id: '', shortname: 'undisclosed', emoji: '👤', initials: 'A'};
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
    this.logger.debug(`${this.className}.login`, location.origin, this.location.prepareExternalUrl('oauth2/authorization/cognito'));
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

  private hasRole(role: AuthRole) {
    return this.currentUserSubject.value
      && this.currentUserSubject.value.roles
      && (this.currentUserSubject.value.roles.indexOf(role) !== -1);
  }

}
