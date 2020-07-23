import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {

  // A BehaviorSubject is an Observable with a default value
  public isLoading = new BehaviorSubject(false);

  constructor() { }

  setLoading(state:boolean) {
    this.isLoading.next(state);
  }
}
