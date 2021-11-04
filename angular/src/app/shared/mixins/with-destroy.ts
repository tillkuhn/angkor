import {OnDestroy} from '@angular/core';
import {Subject} from 'rxjs';
import {Constructor} from '@angular/cdk/table';

/*
export interface HasLogger {
  logger: NGXLogger;
}
*/

/**
 * Mixin that can be used to unsubscribe from pending subscriptions in your ComponentClass, e.g.
 *
 * export class LocationsComponent extends WithDestroy() implements OnDestroy, OnInit {
 * (...)
 *  this.keyUp$.pipe(
 *    (...)
 *    takeUntil(this.destroy$), // avoid leak https://stackoverflow.com/a/41177163/4292075
 *
 *    See README.adoc for lots of resources on using mixin pattern in typescript
 */
export function WithDestroy<T extends Constructor</* HasLogger */{}>>(Base: T = (class {} as any)) {
  return class extends Base implements OnDestroy /*, HasLogger */ {

    destroy$  = new Subject<void>();

    constructor(...args: any[]) {
      super(...args);
     // console.log(args); // contains all constructor args
    }

    ngOnDestroy() {
      // this.logger.debug('WithDestroy: Component closed, completing destroy$');
      // console.debug('destroy');
      this.destroy$.next();
      this.destroy$.complete();
    }
  };
}
