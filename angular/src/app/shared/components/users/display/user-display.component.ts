import {Component, Input} from '@angular/core';
import {AuthService} from '@shared/services/auth.service';

@Component({
    selector: 'app-user-display',
    templateUrl: './user-display.component.html',
    styleUrls: ['./user-display.component.scss'],
    standalone: false
})
export class UserDisplayComponent {

  @Input() userId: string;

  constructor(public authService: AuthService) {
  }

}
