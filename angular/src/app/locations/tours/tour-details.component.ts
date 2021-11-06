import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {NGXLogger} from 'ngx-logger';
import {FormBuilder} from '@angular/forms';
import {EntityMetadata, EntityType, EntityTypeInfo, ManagedEntity} from '@shared/domain/entities';
import {AuthService} from '@shared/services/auth.service';
import {LocationDetailsComponent} from '@app/locations/location-details.component';
import {Tour} from '@domain/location';
import {TourStoreService} from '@app/locations/tours/tour-store.service';

@Component({
  selector: 'app-tour-details',
  templateUrl: '../location-details.component.html', // todo move template to shared section
  styleUrls: []
})
export class TourDetailsComponent extends LocationDetailsComponent<Tour> implements OnInit {

  entityTypeInfo(): EntityTypeInfo {
    return EntityMetadata[EntityType.TOUR];
  }

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: ManagedEntity, // TODO use Dialog data specific object, but ManagedEntity at least supports id
    public store: TourStoreService,
    public authService: AuthService, // used in form to check if edit is allowed
    public dialogRef: MatDialogRef<TourDetailsComponent>,
    protected formBuilder: FormBuilder,
    protected logger: NGXLogger
  ) {
    super(data, dialogRef, store, formBuilder, logger);
  }

  ngOnInit(): void {
    super.ngOnInit();
  }

}
