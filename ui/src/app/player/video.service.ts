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
  coordinates?: number[];

  //
  // "id": "8dc5d7a3-40c3-4ba5-926c-974d01acd9b5",
  // "name": "hase2",
  // "linkUrl": "http://",
  // "mediaType": "VIDEO",
  // "createdAt": "2021-03-11T18:40:13",
  // "createdBy": "00000000-0000-0000-0000-000000000001",
  // "authScope": "PUBLIC"
}

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
    const operation = 'VideoService.getVideos';
    // Cache it once if vids value is false
    if (!this.video$) {
      const t0 = performance.now();
      this.logger.debug(`${operation} cache is empty, loading from server`);
      this.video$ = this.getApiVideo$().pipe(
        // Extract youtube id  "linkUrl": "https://www.youtube.com/watch?v=1j45454",
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

  private getApiVideo$(): Observable<Video[]> {
    return this.http.get<Video[]>(environment.apiUrlRoot + '/links/videos');
  }

  // Clear video cache
  clearCache() {
    this.video$ = null;
    this.logger.debug(`VideoService.clearCache: cache has been cleared`);
  }

  // private getMockVideo$(): Observable<Video[]> {
  // https://riptutorial.com/rxjs/example/26490/caching-http-responses
  // return of(VIDEOS).pipe(delay(500)); // emitted after delay (like the real http server)

}
