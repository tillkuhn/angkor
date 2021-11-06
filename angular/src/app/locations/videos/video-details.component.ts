import {AuthService} from '@shared/services/auth.service';
import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {LocationDetailsComponent} from '@app/locations/location-details.component';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {EntityMetadata, EntityType, EntityTypeInfo, ManagedEntity} from '@shared/domain/entities';
import {NGXLogger} from 'ngx-logger';
import {Video} from '@domain/location';
import {VideoStoreService} from '@app/locations/videos/video-store.service';

@Component({
  selector: 'app-video-details',
  templateUrl: '../location-details.component.html',
  styleUrls: [] // './video-details.component.scss'
})
export class VideoDetailsComponent extends LocationDetailsComponent<Video> implements OnInit {

  entityTypeInfo(): EntityTypeInfo {
    return EntityMetadata[EntityType.VIDEO];
  }

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: ManagedEntity, // TODO use Dialog data specific object, but ManagedEntity at least supports id
    public store: VideoStoreService,
    public authService: AuthService, // used in form to check if edit is allowed
    public dialogRef: MatDialogRef<VideoDetailsComponent>,
    protected formBuilder: FormBuilder,
    protected logger: NGXLogger
  ) {
    super(data, dialogRef, store, formBuilder, logger);
  }

  ngOnInit(): void {
    super.ngOnInit();
  }
}
