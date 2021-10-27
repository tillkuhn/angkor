import {Component} from '@angular/core';
import {AuthService} from '@shared/services/auth.service';

@Component({
  selector: 'app-my-profile',
  templateUrl: './my-profile.component.html',
  styleUrls: ['../shared/components/common.component.scss']
})
export class MyProfileComponent {


  constructor(public authService: AuthService) {
  }

}
