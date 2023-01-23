import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MasterDataService} from '@shared/services/master-data.service';
import {NGXLogger} from 'ngx-logger';
import {AuthService} from '@shared/services/auth.service';
import {Dish} from '@app/domain/dish';
import {
  ConfirmDialogComponent,
  ConfirmDialogModel,
  ConfirmDialogResult
} from '@shared/components/confirm-dialog/confirm-dialog.component';
import {MatLegacyDialog as MatDialog} from '@angular/material/legacy-dialog';
import {DishStoreService} from '../dish-store.service';

@Component({
  selector: 'app-dish-detail',
  templateUrl: './dish-detail.component.html',
  styleUrls: ['../../shared/components/common.component.scss']
})
export class DishDetailComponent implements OnInit {

  item: Dish;

  constructor(private route: ActivatedRoute,
              public store: DishStoreService,
              public masterData: MasterDataService,
              public authService: AuthService,
              private dialog: MatDialog,
              private router: Router,
              private logger: NGXLogger) {
  }

  ngOnInit() {
    this.getItem(this.route.snapshot.params.id);
  }

  getItem(id: any) {
    this.store.getItem(id)
      .subscribe((data: any) => {
        this.item = data;
        // this.logger.debug('getItem()', this.item);
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
      if ((dialogResult as ConfirmDialogResult).confirmed) {
        this.deleteItem(item.id);
      }
    });
  }

  deleteItem(id: string) {
    this.logger.debug(`Deleting ${id}`);
    this.store.deleteItem(id)
      .subscribe({
        next: _ => {
          this.router.navigate(['/dishes']).then();
        }
        , error:
          (err) => {
            this.logger.error('deleteItem', err);
          }
      });
  }

  // END Delete Section


  justServed() {
    this.store.justServed(this.item.id)
      .subscribe((data: any) => {
        this.item.timesServed = data.result;
        this.logger.debug('justServed()', data.result);
      });
  }

}
