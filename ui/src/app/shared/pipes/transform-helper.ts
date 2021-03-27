/**
 * Static helpers for transformation so we can use them out of pipes
 */
export class TransformHelper {

  // from https://github.com/angular/angular.io/blob/master/public/docs/_examples/testing/ts/src/app/shared/title-case.pipe.ts
  static titleCase(input: string): string {
    return input.length === 0 ? '' :
      input.replace(/\w\S*/g, (txt => txt[0].toUpperCase() + txt.substr(1).toLowerCase()));
  }
}
