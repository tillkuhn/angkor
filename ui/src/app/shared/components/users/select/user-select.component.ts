import {Component, Input, OnInit} from '@angular/core';
import {AuthService} from '../../../services/auth.service';
import {FormGroup} from '@angular/forms';

@Component({
  selector: 'app-user-select',
  templateUrl: './user-select.component.html',
  styleUrls: ['./user-select.component.scss']
})
export class UserSelectComponent implements OnInit {

  @Input() parentForm: FormGroup;
  @Input() parentFormTagsControlName = 'userId';

  @Input() userId: string;
  @Input() label = 'User';

  constructor(public authService: AuthService) { }

  ngOnInit(): void {
  }

}
