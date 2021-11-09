import {Component, ViewChild} from '@angular/core';
import {TourStoreService} from '@app/locations/tours/tour-store.service';
import {LocationDetailsComponent} from '@app/locations/location-details.component';
import {NGXLogger} from 'ngx-logger';
import {Location, Tour} from '@domain/location';
import {ImagineService} from '@shared/modules/imagine/imagine.service';
import {FileItem} from '@shared/modules/imagine/file-item';
import {AuthService} from '@shared/services/auth.service';

@Component({
  selector: 'app-tour-details',
  templateUrl: './tour-details.component.html',
  styleUrls: []
})
export class TourDetailsComponent {

  private readonly className = 'TourDetailsComponent';

  // https://indepth.dev/posts/1415/implementing-reusable-and-reactive-forms-in-angular-2
  // Note how the @ViewChild options uses the static: true to resolve the component instance as soon as possible
  // so that we have the sub-form instance of the LocationDetailsComponent class.
  @ViewChild(LocationDetailsComponent, { static: true })
  public locationDetails: LocationDetailsComponent<Tour>;
  item: Tour;

  constructor(
    public store: TourStoreService,
    private logger: NGXLogger,
    private imagine: ImagineService,
    private auth: AuthService,
  ) {}


  // Just to test access to child class
  downloadGPX(): string {
    const externalId = this.locationDetails.formData?.get('externalId')?.value;
    return `https://www.komoot.de/api/v007/tours/${externalId}.gpx`;
  }

  itemLoaded(event: Location) {
    this.logger.info(`${this.className}: Tour ${event.id} loaded`);
    this.item = event as Tour;

    // check current attachment for tour
    this.imagine.getEntityFiles(this.store.entityType(), this.item.id)
      .subscribe((files: FileItem[]) => {
        this.logger.debug(`${this.className}.loadFiles: ${files ? files.length : 0} files found`);
        this.checkGPXCopy(files);
      }, err => {
        this.logger.error(`${this.className}.loadFiles: error while reading files - ${err}`);
      });
  }

  checkGPXCopy(files: FileItem[]) {
    const gpxExists = files && files.some(file => file.filename?.toLowerCase().endsWith('.gpx'));
    if (gpxExists) {
      this.logger.debug(`${this.className}: GPX Backup already exists`);
    } else if (! this.auth.canEdit) {
      this.logger.debug(`${this.className}: gpxExists=${gpxExists} but we're not authenticated / not allowed to upload`);
    } else if (! gpxExists) {
      this.logger.debug(`No gpx backup found for ${this.item.id}, triggering download on behalf of ${this.auth.currentUser.login}`);
      const uploadRequest = {
        entityType: this.item.entityType,
        entityId: this.item.id,
        url: this.downloadGPX(),
        filename: `tour-${this.item.externalId}.gpx`,
      };
      this.imagine
        .uploadUrl(uploadRequest, this.item.entityType, this.item.id)
        .subscribe( res => this.logger.debug(`${this.className}: Upload result ${res}`));
    }
  }

}
