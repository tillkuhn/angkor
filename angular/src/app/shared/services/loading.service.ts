import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {

  // A BehaviorSubject is an Observable with a default value
  // create customize Observer-side logic of the Subject and conceal it from code that uses the Observable.
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  isLoading$: Observable<boolean> = this.isLoadingSubject; // .asObservable(); use Typecast instead

  constructor() {
  }

  setLoading(state: boolean) {
    this.isLoadingSubject.next(state);
  }

}
