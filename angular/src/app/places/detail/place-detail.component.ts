import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NGXLogger} from 'ngx-logger';
import {Place} from '@app/domain/place';
import {MasterDataService} from '@shared/services/master-data.service';
import {SmartCoordinates} from '@shared/domain/smart-coordinates';
import {MatLegacyDialog as MatDialog} from '@angular/material/legacy-dialog';
import {ConfirmDialogComponent, ConfirmDialogModel, ConfirmDialogResult} from '@shared/components/confirm-dialog/confirm-dialog.component';
import {AuthService} from '@shared/services/auth.service';
import {PlaceStoreService} from '../place-store.service';

@Component({
  selector: 'app-place-detail',
  templateUrl: './place-detail.component.html',
  styleUrls: ['../../shared/components/common.component.scss']
})
export class PlaceDetailComponent implements OnInit {

  place: Place = {id: '', name: '', areaCode: ''};
  coordinates: SmartCoordinates;
  deleteDialogResult = '';

  constructor(private route: ActivatedRoute,
              private store: PlaceStoreService,
              private router: Router,
              private logger: NGXLogger,
              private dialog: MatDialog,
              public masterData: MasterDataService,
              public authService: AuthService) {
  }

  ngOnInit() {
    this.getPlaceDetails(this.route.snapshot.params.id);
  }

  getPlaceDetails(id: any) {
    this.store.getItem(id)
      .subscribe((data: any) => {
        this.place = data;
        if (this.place?.coordinates?.length > 1) {
          this.coordinates = new SmartCoordinates(this.place.coordinates);
        }
        this.logger.trace('getPlaceDetails()', this.place);
      });
  }

  deletePlace(id: string) {
    this.logger.debug(`Deleting ${id}`);
    this.store.deleteItem(id)
      .subscribe({
        next: _ => {
          this.router.navigate(['/places']).then(); // warning if we omit then()
        }
        ,
        error: (err) => {
          this.logger.error('deletePlace error: ', err);
        }
      });
  }

  confirmDeleteDialog(place: Place): void {
    const message = `Are you sure you want to do trash ${place.name}?`;
    const dialogData = new ConfirmDialogModel('Confirm Action', message);
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      maxWidth: '400px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(dialogResult => {
      if ((dialogResult as ConfirmDialogResult).confirmed) {
        this.deletePlace(place.id);
      }
    });
  }

}
