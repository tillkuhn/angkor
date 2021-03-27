import {Injectable} from '@angular/core';
import {EntityStore} from '@shared/services/entity-store';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType} from '@shared/domain/entities';
import {ApiHelper} from '@shared/helpers/api-helper';
import {ApiLink, Link} from '@domain/link';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {map, publishReplay, refCount, tap} from 'rxjs/operators';
import {EntityEventService} from '@shared/services/entity-event.service';

@Injectable({
  providedIn: 'root'
})
export class LinkStoreService extends EntityStore<Link, ApiLink> {

  constructor(http: HttpClient,
              logger: NGXLogger,
              entityEvents: EntityEventService,
  ) {
    super(http, logger, entityEvents);
    entityEvents.observe(EntityType.LINK)
      .subscribe(event => {
        logger.info(`${this.className}.entityEvents: Received event ${event.action} ${event.entityType}`);
        this.clearCache();
      });
  }

  // Extension for "special" link Video
  private video$: Observable<Link[]>;

  /**
   * Subscribe to a list of Links that qualify as Videos
   */
  getVideo$(): Observable<Link[]> {
    const operation = `${this.className}.getVideo$`;
    // Cache it once if vids value is false
    if (!this.video$) {
      const t0 = performance.now();
      this.logger.debug(`${operation} cache is empty, loading from server`);
      this.video$ = this.getApiVideo$().pipe(
        // Extract youtube id  "linkUrl": "https://www.youtube.com/watch?v=1j45454",
        map<Link[], Link[]>(videos =>
          videos.map(video => {
            // Try to extract youtube id from supported URL formats
            if (video.linkUrl?.startsWith('https://www.youtube.com') ) {
              video.youtubeId = video.linkUrl.split('=').pop();
            } else if (video.linkUrl?.startsWith('https://youtu.be'))  {
              video.youtubeId = video.linkUrl.split('/').pop();
            } else {
              this.logger.warn(`${this.className}.getVideos: Can't extract youtubeId from ${video.linkUrl}`);
            }
            return video;
          })
        ),
        tap(_ => this.logger.debug(`${operation}: successfully fetched items in ${Math.round(performance.now() - t0)} millis`)),
        publishReplay(1), // this tells Rx to cache the latest emitted
        refCount() // and this tells Rx to keep the Observable alive as long as there are any Subscribers
      );
    }
    return this.video$;
  }
  private getApiVideo$(): Observable<Link[]> {
    return this.http.get<Link[]>(environment.apiUrlRoot + '/links/videos');
  }

  // Clear video cache
  private clearCache() {
    this.video$ = null;
    this.logger.debug(`${this.className}.clearCache: cache has been cleared`);
  }

 // Standard entityStopre

  // must override
  entityType(): EntityType {
    return EntityType.LINK;
  }

  // override standard mapper in superclass
  mapFromApiEntity(apiEntity: ApiLink): Link {
    return {
      ...apiEntity,
      createdAt: ApiHelper.parseISO(apiEntity.createdAt),
    };
  }

  // override standard mapper in superclass
  protected mapToApiEntity(uiEntity: Link): ApiLink {
    // https://ultimatecourses.com/blog/remove-object-properties-destructuring
    const {
      createdAt, // remove
      ...rest  // ... what remains
    } = uiEntity;
    return {
      ...rest,
    };
  }
}
