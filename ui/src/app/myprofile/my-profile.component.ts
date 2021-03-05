import {Component, OnInit} from '@angular/core';
import {AuthService} from '../shared/services/auth.service';
import {User} from '../domain/user';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-my-profile',
  templateUrl: './my-profile.component.html',
  styleUrls: ['./my-profile.component.scss']
})
export class MyProfileComponent implements OnInit {

  currentUser$: Observable<User>;

  constructor(private auhtService: AuthService) {
  }

  ngOnInit(): void {
    this.currentUser$ = this.auhtService.currentUser$;
  }

}
