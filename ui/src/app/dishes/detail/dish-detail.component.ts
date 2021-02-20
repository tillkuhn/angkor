import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ApiService} from '../../shared/api.service';
import {MasterDataService} from '../../shared/master-data.service';
import {NGXLogger} from 'ngx-logger';
import {AuthService} from '../../shared/auth.service';
import {Dish} from '../../domain/dish';
import {ConfirmDialogComponent, ConfirmDialogModel} from '../../shared/components/confirm-dialog/confirm-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-dish-detail',
  templateUrl: './dish-detail.component.html',
  styleUrls: ['./dish-detail.component.scss']
})
export class DishDetailComponent implements OnInit {

  item: Dish;

  constructor(private route: ActivatedRoute, private api: ApiService,
              public masterData: MasterDataService, public authService: AuthService,
              private dialog: MatDialog, private snackBar: MatSnackBar,
              private router: Router, private logger: NGXLogger) {
  }

  ngOnInit() {
    this.getItem(this.route.snapshot.params.id);
  }

  getItem(id: any) {
    this.api.getDish(id)
      .subscribe((data: any) => {
        this.item = data;
        this.logger.debug('getItem()', this.item);
      });
  }

  // BEGIN Delete Section, todo make more reusable
  confirmDeleteDialog(item: Dish): void {
    const message = `Are you sure you want to do trash ${item.name}?`;
    const dialogData = new ConfirmDialogModel('Confirm Action', message);
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      maxWidth: '400px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(dialogResult => {
      if (dialogResult) {
        this.deleteItem(item.id);
      }
    });
  }

  deleteItem(id: string) {
    this.logger.debug(`Deleting ${id}`);
    this.api.deleteDish(id)
      .subscribe(res => {
          this.snackBar.open('Item was successfully trashed', 'Close');
          this.router.navigate(['/dishes']);
        }, (err) => {
          this.logger.error('deleteItem', err);
        }
      );
  }

  // END Delete Section


  justServed() {
    this.api.justServed(this.item.id)
      .subscribe((data: any) => {
        this.item.timesServed = data.result;
        this.logger.debug('justServed()', data.result);
      });
  }

}
