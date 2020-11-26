import {Injectable, VERSION} from '@angular/core';

/* hack from https://github.com/angular/angular-cli/issues/3855#issuecomment-579719646
* use index.html to envsubst post build / runtime values
*/
@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {

  version: string;
  mapboxAccessToken: string;
  imprintUrl: string;
  // https://github.com/angular/angular/issues/1357#issuecomment-346084639
  angularVersion = VERSION.full; // e.g. 10.0.7

  constructor() {
    const windowEnv = (window as any).env;
    // console.log(this.angularVersion);
    this.version = windowEnv && windowEnv.VERSION !== '' && windowEnv.VERSION !== '${VERSION}' ? windowEnv.VERSION : 'latest';
    this.mapboxAccessToken = windowEnv && windowEnv.MAT !== '' && windowEnv.MAT !== '${MAT}' ? windowEnv.MAT : 'no-token';
    this.imprintUrl = windowEnv && windowEnv.IMPRINT_URL !== '' && windowEnv.IMPRINT_URL !== '${IMPRINT_URL}' ? windowEnv.IMPRINT_URL : '404';
  }

}
