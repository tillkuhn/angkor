import {Component, Input, OnInit} from '@angular/core';
import {AuthService} from '@shared/services/auth.service';

@Component({
  selector: 'app-user-display',
  templateUrl: './user-display.component.html',
  styleUrls: ['./user-display.component.scss']
})
export class UserDisplayComponent implements OnInit {

  @Input() userId: string;

  constructor(public authService: AuthService) {
  }

  ngOnInit(): void {
  }

}
