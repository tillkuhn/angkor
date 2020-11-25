import {Component, OnInit} from '@angular/core';
import {AuthService} from '../shared/auth.service';
import {User} from '../domain/user';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {

  currentUser$: Observable<User>;

  constructor(private auhtService: AuthService) {
  }

  ngOnInit(): void {
    this.currentUser$ = this.auhtService.currentUser$;
  }

}
