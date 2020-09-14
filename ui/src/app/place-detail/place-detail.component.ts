import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ApiService} from '../shared/api.service';
import {NGXLogger} from 'ngx-logger';
import {Place} from '../domain/place';
import {MasterDataService} from '../shared/master-data.service';
import {SmartCoordinates} from '../domain/smart-coordinates';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmDialogComponent, ConfirmDialogModel} from '../shared/components/confirm-dialog/confirm-dialog.component';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '../shared/auth.service';

@Component({
  selector: 'app-place-detail',
  templateUrl: './place-detail.component.html',
  styleUrls: ['./place-detail.component.scss']
})
export class PlaceDetailComponent implements OnInit {

  place: Place = {id: '', name: '', areaCode: ''};
  coordinates: SmartCoordinates;
  deleteDialogResult = '';

  constructor(private route: ActivatedRoute, private api: ApiService, public masterData: MasterDataService,
              private router: Router, private logger: NGXLogger, private dialog: MatDialog,
              private snackBar: MatSnackBar, public authService: AuthService) {
  }

  ngOnInit() {
    this.getPlaceDetails(this.route.snapshot.params.id);
  }

  getPlaceDetails(id: any) {
    this.api.getPlace(id)
      .subscribe((data: any) => {
        this.place = data;
        if (this.place.coordinates && this.place.coordinates.length > 1) {
          this.coordinates = new SmartCoordinates(this.place.coordinates);
        }
        this.logger.debug('getPlaceDetails()', this.place);
      });
  }

  deletePlace(id: string) {
    this.logger.debug(`Deleting ${id}`);
    this.api.deletePlace(id)
      .subscribe(res => {
          this.snackBar.open('Place was successfully trashed', 'Close');
          this.router.navigate(['/places']);
        }, (err) => {
          this.logger.error('deletePlace', err);
        }
      );
  }

  confirmDeleteDialog(place: Place): void {
    const message = `Are you sure you want to do trash ${place.name}?`;
    const dialogData = new ConfirmDialogModel('Confirm Action', message);
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      maxWidth: '400px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(dialogResult => {
      if (dialogResult) {
        this.deletePlace(place.id);
      }
    });
  }

}
