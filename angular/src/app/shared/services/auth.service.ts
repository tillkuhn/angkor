import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Location} from '@angular/common';
import {NGXLogger} from 'ngx-logger';
import {PRE_LOGIN_URL_SESSION_KEY} from '../guards/hilde.guard';
import {Router} from '@angular/router';
import {User, UserSummary} from '@app/domain/user';
import {WebStorageService} from 'ngx-web-storage';
import {environment} from '../../../environments/environment';
import {map, share, shareReplay} from 'rxjs/operators';

// import { AuthServerProvider } from 'app/core/auth/auth-session.service';

declare type AuthRole = 'ROLE_USER' | 'ROLE_ADMIN';

export interface Authentication {
  idToken?: string;
  authenticated: boolean;
  user?: User;
}

/**
 * Persisting user authentication with BehaviorSubject in Angular
 * https://netbasal.com/angular-2-persist-your-login-status-with-behaviorsubject-45da9ec43243
 */
@Injectable({providedIn: 'root'})
export class AuthService {

  private readonly className = 'AuthService';
  private userSummaryLookup: Map<string, UserSummary> = new Map();

  private anonymousAuthentication: Authentication = {authenticated: false};
  private authenticationSubject = new BehaviorSubject<Authentication>(this.anonymousAuthentication);

  // web store: https://stackblitz.com/edit/ngx-web-storage?file=app%2Fapp.component.ts
  constructor(
    private http: HttpClient,
    private logger: NGXLogger,
    private location: Location,
    private router: Router,
    private storage: WebStorageService) {
    this.checkAuthentication(); // check if authenticated, and if so - load the user
  }

  // A subject in Rx is both Observable and Observer.
  // In this case, we only expose the Observable part
  get authentication$(): Observable<Authentication> {
    return this.authenticationSubject.asObservable().pipe(share());
  }

  isAuthenticated$: Observable<boolean> = this.authenticationSubject
    .pipe(
      map(result => result.authenticated),
      shareReplay(),
    );

  // ... and the sync versions, returns last value of the subject
  get isAuthenticated(): boolean {
    return this.authenticationSubject.value.authenticated;
  }

  get currentUser(): User {
    return this.authenticationSubject.value.user;
  }

  get idToken(): string {
    return this.authenticationSubject.value.idToken;
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
   * Central function to check if a role is present (handles currentUser == null gracefully)
   */
  private hasRole(role: AuthRole): boolean {
    // this.currentUserSubject.value is null if unauthenticated
    const roles = this.authenticationSubject.value?.user?.roles;
    return roles && roles.indexOf(role) !== -1;
  }

  /**
   * Asks the backend via rest if current user is authenticated (!= anonymous), returns boolean resp.
   * if true, also loads details of current user (/account)
   */
  checkAuthentication() {
    const operation = `${this.className}.checkAuthentication`;
    this.http.get<Authentication>(environment.apiUrlRoot + '/authentication')
      .subscribe(authResponse => {
        this.logger.debug(`${operation} Got authentication authenticated=${authResponse.authenticated} user=${authResponse.user}`);
        this.authenticationSubject.next(authResponse); // if true, we also get idToken ans User

        if (authResponse.authenticated) { // means yes - we are authenticated
          // this.currentUserSubject.next(authResponse.user);
          // authenticated users are also allowed to see user user summary (e.g. nickname), so we load them
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

  /**
   * Returns array of known userSummaries by converting the internal map,
   * e.g. for userSelect component
   */
  userSummaries(): UserSummary[] {
    const us: UserSummary[] = [];
    this.userSummaryLookup.forEach((value: UserSummary, _: string) => {
      us.push(value);
    });
    return us;
  }

  /**
   * Returns the summary by id, or undisclosed if id is not part of the map
   * @param userId the internal user id
   */
  lookupUserSummary(userId: string): UserSummary {
    const us = this.userSummaryLookup.get(userId);
    return us ? us : {id: '', shortname: 'undisclosed', emoji: '👤', initials: 'A'};
  }

  /**
   *  Called by login button, triggers the OIDC login process
   *  by invoking external oauth2/authorization endpoint
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
   * Called by logout button  authService.logout() click action
   */
  logout() {
    this.logger.warn('logout user ');
    this.authenticationSubject.next(this.anonymousAuthentication);
    this.storage.session.remove(PRE_LOGIN_URL_SESSION_KEY); // used for redirect after login
    this.http.post(`${environment.apiUrlRoot}/logout`, {}, {observe: 'response'}).subscribe(
      response => {
        // const data = response.body; // returns logoutUrl null and idToken
        this.logger.info(`${environment.apiUrlRoot}/logout returned ${response}`);
        this.router.navigate(['/logout']).then();
      }
      // map((response: HttpResponse<any>) => {
      // to get a new csrf token call the api
      // this.http.get(SERVER_API_URL + 'api/account').subscribe(() => {}, () => {});
      // return response;
      // })
    );
  }

}
