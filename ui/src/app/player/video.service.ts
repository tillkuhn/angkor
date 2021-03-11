import { Injectable } from '@angular/core';
import {Observable, of} from 'rxjs';
import {delay, map, publishReplay, refCount, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {environment} from '../../environments/environment';


export interface Video {
  id?: string;
  name: string;
  linkUrl?: string;
  youtubeId?: string;
  mediaType?: string;
  createdAt?: string | Date;
  createdBy?: string;
  authScope?: string;

  //
  // "id": "8dc5d7a3-40c3-4ba5-926c-974d01acd9b5",
  // "name": "hase2",
  // "linkUrl": "http://",
  // "mediaType": "VIDEO",
  // "createdAt": "2021-03-11T18:40:13",
  // "createdBy": "00000000-0000-0000-0000-000000000001",
  // "authScope": "PUBLIC"
}

const VIDEOS: Video[] = [
  {
    youtubeId: '11cA8h2YAZQ',
    name: 'Devil\'s Tears on Nusa Lembongan',
  },
  {
    youtubeId: 'nScz_nNwUl8',
    name: 'Fireshow @ Ume Cafe, Ngwe Saung',
  },
  {
    youtubeId: 'PBlrX41ot7c',
    name: 'Flower Power @Spring River Kong Lor',
  },
  {
    youtubeId: 'S8kvEf50Xvo',
    name: 'Sabaidee Pi Mai Lao',
  },
  {
    youtubeId: 'LtfS5hgkvt8',
    name: 'Buffalo herd traffic jam near Pak Ou',
  },
  {
    youtubeId: 'AiZQU5T8jLk',
    name: 'Bridge Nahm Dong Park',
  },
  {
    youtubeId: 'qtHuIYtR1lw',
    name: 'El Fortin Canopy Zipline',
  },
  {
    youtubeId: '1jdEV5yy0GA',
    name: 'Shentang Gulf Views',
  },
  {
    youtubeId: 'o-iHevA9KP0',
    name: 'Bamboo Train Ride Cambodia',
  }
];

/**
 * Input for cache:
 * - https://indepth.dev/posts/1248/fastest-way-to-cache-for-lazy-developers-angular-with-rxjs
 * - https://riptutorial.com/rxjs/example/26490/caching-http-responses
 */
@Injectable({
  providedIn: 'root'
})
export class VideoService {

  private video$: Observable<Video[]>;

  constructor(private http: HttpClient, private logger: NGXLogger) { }

  getVideo$(): Observable<Video[]> {
    const operation = 'VideoService.getVids';
    // Cache it once if vids value is false
    // this.vids = this.httpClient.get(`${api_url}/vids`).pipe(
    if (!this.video$) {
      const t0 = performance.now();
      this.logger.debug(`${operation} cache is empty, loading from server`);
      this.video$ = this.getApiVideo$().pipe(
        // Extraxt youtube id  "linkUrl": "https://www.youtube.com/watch?v=1j45454",
        map<Video[], Video[]>(videos =>
          videos.map(video => {
            video.youtubeId = video.linkUrl?.startsWith('https://www.youtube.com') ? video.linkUrl.split('=').pop() : null;
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

  // for later ater backend impl
  getApiVideo$(): Observable<Video[]> {
    return this.http.get<Video[]>(environment.apiUrlRoot + '/videos');
  }

  // Clear video cache
  clearCache() {
    this.video$ = null;
    this.logger.debug(`VideoService.clearCache: cache has been cleared`);
  }

  private getMockVideo$(): Observable<Video[]> {
    // https://riptutorial.com/rxjs/example/26490/caching-http-responses
    return of(VIDEOS).pipe(
      delay(500) // emitted after delay (like the real http server)
    );
  }

}
