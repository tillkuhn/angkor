import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {AuthService} from '@shared/services/auth.service';
import {NGXLogger} from 'ngx-logger';

@Component({
  selector: 'app-tour-details',
  templateUrl: './tour-details.component.html',
  styleUrls: ['./tour-details.component.scss']
})
export class TourDetailsComponent implements OnInit {

  private readonly className = 'LinkDetailsComponent';

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public authService: AuthService, // used in form to check if edit is allowed
    public dialogRef: MatDialogRef<TourDetailsComponent>,
    // private formBuilder: FormBuilder,
    // private linkService: LinkStoreService,
    private logger: NGXLogger
  ) {
  }

  ngOnInit(): void {
    this.logger.debug(`${this.className}.ngOnInit(): Warming up`);
  }

  closeItem(): void {
    this.dialogRef.close(); // no arg will be considered as cancel
  }

}
