import { Injectable } from '@angular/core';
/* hack from https://github.com/angular/angular-cli/issues/3855#issuecomment-579719646
* use index.html to envsubst post build / runtime values
*/
@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  version: string;
  mapboxAccessToken: string;
  constructor() {
    const windowEnv = (window as any).env;
    this.version = windowEnv.VERSION !== '' && windowEnv.VERSION !== '${VERSION}' ? windowEnv.VERSION : 'latest';
    this.mapboxAccessToken = windowEnv.MAT !== '' && windowEnv.MAT !== '${MAT}' ? windowEnv.MAT : 'no-token';
  }

}
