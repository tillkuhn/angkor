import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {AuthService} from '@shared/services/auth.service';
import {FormGroup} from '@angular/forms';
import {NGXLogger} from 'ngx-logger';

@Component({
  selector: 'app-user-select',
  templateUrl: './user-select.component.html',
  styleUrls: ['./user-select.component.scss']
})
export class UserSelectComponent implements OnInit {

  @Input() formGroup: FormGroup;
  // Must not be named 'fromControlName' which causes errors (probably conflict) :-(
  @Input() controlName = 'userId';

  @Input() userId: string;
  @Input() label = 'User';

  constructor(private logger: NGXLogger,
              public authService: AuthService) { }

  ngOnInit(): void {
    if (this.formGroup == null) {
      throw new Error('[formGroup] must be set');
    }
    // should return AbstractControl
    if (! this.formGroup.get(this.controlName)) {
      this.logger.warn(`formControlName ${this.controlName} not (yet) present in form group`);
    }
  }

}
