import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {

  // A BehaviorSubject is an Observable with a default value
  // create customize Observer-side logic of the Subject and
  // conceal it from code that uses the Observable.
  private isLoadingSubject = new BehaviorSubject(false);
  isLoading$ = this.isLoadingSubject.asObservable();

  constructor() {
  }

  setLoading(state: boolean) {
    this.isLoadingSubject.next(state);
  }
}
