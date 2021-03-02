import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EntityType} from '../../../domain/entities';
import {Observable} from 'rxjs';
import {TagSummary} from '../../../domain/tag';
import {EntityHelper} from '../../entity-helper';
import {map, tap} from 'rxjs/operators';
import {NGXLogger} from 'ngx-logger';

@Injectable({
  providedIn: 'root'
})
export class TagService {

  private readonly className = 'TagService';

  constructor(private http: HttpClient,
              private logger: NGXLogger) {
  }

  entityTags(entityType: EntityType): Observable<string[]> {
    const apiUrl = EntityHelper.getApiUrl(EntityType.Tag); // e.g. places
    this.logger.debug(`${this.className}.entityTages pull from ${apiUrl}/${entityType}`);
    return this.http.get<TagSummary[]>(`${apiUrl}/${entityType}`)
      .pipe(
        map<TagSummary[], string[]>(items =>
          items.map(item => `${item.label} (${item.count})`)
        ),
        tap(tags => this.logger.debug(`${this.className}.entityTags for ${entityType}: ${tags.length}`)),
        // TODO catchError(this.handleError('getFiles', []))
      );
  }

}