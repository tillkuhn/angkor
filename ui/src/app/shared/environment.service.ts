import {Injectable, VERSION} from '@angular/core';

/**
 * hack from https://github.com/angular/angular-cli/issues/3855#issuecomment-579719646
 * use index.html to envsubst post build / runtime values
 */
@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {

  appVersion: string;
  mapboxAccessToken: string;
  imprintUrl: string;
  angularVersion = VERSION.full; // e.g. 10.0.7 see https://github.com/angular/angular/issues/1357#issuecomment-346084639

  constructor() {
    const windowEnv = (window as any).env;
    // console.log(this.angularVersion);
    this.appVersion = windowEnv && windowEnv.APP_VERSION !== '' && windowEnv.APP_VERSION !== '${APP_VERSION}' ? windowEnv.APP_VERSION : 'latest';
    this.mapboxAccessToken = windowEnv && windowEnv.MAT !== '' && windowEnv.MAT !== '${MAT}' ? windowEnv.MAT : 'no-token';
    this.imprintUrl = windowEnv && windowEnv.IMPRINT_URL !== '' && windowEnv.IMPRINT_URL !== '${IMPRINT_URL}' ? windowEnv.IMPRINT_URL : '404';
  }

}
