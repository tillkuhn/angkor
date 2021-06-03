import {Component, OnInit} from '@angular/core';
import {AuthService} from '@shared/services/auth.service';

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
