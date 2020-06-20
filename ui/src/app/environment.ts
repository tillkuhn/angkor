import { Injectable } from '@angular/core';
/* hack from https://github.com/angular/angular-cli/issues/3855#issuecomment-579719646
* use index.html to envsubst post build / runtime values
*/
@Injectable({
  providedIn: 'root'
})
export class Environment {
  constructor() {
    const env = (window as any).env;

    this.version = env.version !== '' && env.version !== '${VERSION}' ? env.version : 'latest';
  }

  version: string;
}
