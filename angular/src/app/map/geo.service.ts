import {Injectable} from '@angular/core';
import {EntityMetadata, EntityType} from '@shared/domain/entities';
import {Observable} from 'rxjs';
import {POI} from '@domain/poi';
import {environment} from '../../environments/environment';
import {catchError, tap} from 'rxjs/operators';
import {ApiHelper} from '@shared/helpers/api-helper';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityEventService} from '@shared/services/entity-event.service';

@Injectable({
  providedIn: 'root'
})
export class GeoService {

  // Constants for selected Zoom Levels (detailed -> broad)
  // 10 ~ detailed like bangkok + area, 5 ~ southeast asia, 0 ~ the earth
  // More Details: https://docs.mapbox.com/help/glossary/zoom-level/
  static readonly DEEPLINK_POI_ZOOM = 12; // if called with maps/@lat,lon
  static readonly ON_CLICK_POI_ZOOM = 6; // if poi is clicked
  static readonly DEFAULT_POI_ZOOM = 2; // default when /map is launched w/o args

  protected readonly className = 'GeoService';

  // Remove (longer needed): {description: 'Street',id: 'streets-v11'}
  readonly mapStyles = [
    {
      description: 'Outdoor',
      id: 'outdoors-v11'
    },
    {
      description: 'Satellite',
      id: 'satellite-streets-v11' // 'satellite-v9' is w/o streets
    }
  ];

  // mapStyle holds the current style and is bound via [style] on the ngl-map element
  mapStyle = `mapbox://styles/mapbox/${this.mapStyles[0].id}`; // default outdoor

  constructor(protected http: HttpClient,
              protected logger: NGXLogger,
              protected events: EntityEventService,
  ) {
  }

  // Check if we find a new place ...
  getPOIs(entityType: EntityType): Observable<POI[]> {
    const eMeta = EntityMetadata[entityType];
    const url = `${environment.apiUrlRoot}/pois/${eMeta.path}`;
    this.logger.debug(`AreaStoreService fetched ${url}`);
    return this.http.get<POI[]>(url)
      .pipe(
        tap(pois => this.logger.debug(`ApiService.getPOIs fetched ${pois.length} pois`)),
        catchError(ApiHelper.handleError('getPOIs', this.events, []))
      );
  }
}
