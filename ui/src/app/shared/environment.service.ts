import {Injectable, VERSION} from '@angular/core';
import {parseISO} from 'date-fns';

/**
 * hack from https://github.com/angular/angular-cli/issues/3855#issuecomment-579719646
 * use index.html to envsubst post build / runtime values
 */
@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {

  appVersion: string;
  releaseName;
  string;
  mapboxAccessToken: string;
  imprintUrl: string;
  angularVersion = VERSION.full; // e.g. 10.0.7 see https://github.com/angular/angular/issues/1357#issuecomment-346084639
  uiStarted: Date;

  constructor() {
    // console.log(this.angularVersion);
    this.appVersion = this.resolveVariable('APP_VERSION', 'latest');
    this.mapboxAccessToken = this.resolveVariable('MAT', 'no-mat-token');
    this.imprintUrl = this.resolveVariable('IMPRINT_URL', '404');
    this.releaseName = this.resolveVariable('RELEASE_NAME', 'unknown-species');
    this.uiStarted = parseISO(this.resolveVariable('UI_STARTED', '2000-01-01'));
  }

  resolveVariable(key: string, defaultVal: string) {
    const windowEnv = (window as any).env;
    return windowEnv && windowEnv[key] !== '' && windowEnv[key] !== '${' + key + '}' ? windowEnv[key] : defaultVal;
  }

}
