import {Component, OnInit} from '@angular/core';
import {Authentication, AuthService} from '@shared/services/auth.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-my-profile',
  templateUrl: './my-profile.component.html',
  styleUrls: ['../shared/components/common.component.scss']
})
export class MyProfileComponent implements OnInit {


  constructor(public authService: AuthService) {
  }

  ngOnInit(): void {
  }

}
