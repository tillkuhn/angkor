import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EntityType} from '@shared/domain/entities';
import {Observable} from 'rxjs';
import {TagSummary} from '@app/domain/tag';
import {ApiHelper} from '../../helpers/api-helper';
import {catchError, map, tap} from 'rxjs/operators';
import {NGXLogger} from 'ngx-logger';
import {EntityEventService} from '@shared/services/entity-event.service';

@Injectable({
  providedIn: 'root'
})
export class TagService {

  private readonly className = 'TagService';

  constructor(private http: HttpClient,
              private events: EntityEventService,
              private logger: NGXLogger) {
  }

  entityTags(entityType: EntityType): Observable<string[]> {
    return this.entityTagsRaw(entityType)
      .pipe(
        map<TagSummary[], string[]>(items =>
          items.map(item => `${item.label} (${item.count})`)
        ),
        tap(tags => this.logger.debug(`${this.className}.entityTags for ${entityType}: ${tags.length}`)),
        catchError(ApiHelper.handleError('getAreaTree', this.events, []))
      );
  }

  entityTagsRaw(entityType: EntityType): Observable<TagSummary[]> {
    const apiUrl = ApiHelper.getApiUrl(EntityType.Tag); // e.g. places
    this.logger.debug(`${this.className}.entityTages pull from ${apiUrl}/${entityType}`);
    return this.http.get<TagSummary[]>(`${apiUrl}/${entityType}`);
  }
}
