import {AuthService} from '@shared/services/auth.service';
import {FormControl} from '@angular/forms';
import {NGXLogger} from 'ngx-logger';
import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-user-select',
  templateUrl: './user-select.component.html',
  styleUrls: ['./user-select.component.scss']
})
export class UserSelectComponent implements OnInit {

  // Must not be named 'fromControl' which causes errors (probably conflict) :-(
  @Input() formControlSelect: FormControl;
  @Input() userId: string;
  @Input() label = 'User';

  constructor(public authService: AuthService /* needed to get user Summaries */ ) { }

  ngOnInit(): void {
    if (this.formControlSelect === null) {
      throw Error('formControl for select component must not be null');
    }
  }

}
