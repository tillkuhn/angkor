import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {shareReplay, takeUntil} from 'rxjs/operators';
import {NGXLogger} from 'ngx-logger';
import {ImagineService} from '@shared/modules/imagine/imagine.service';
import {EntityType} from '@shared/domain/entities';

export interface StreamState {
  playing: boolean;
  readableCurrentTime: string;
  readableDuration: string;
  duration: number | undefined;
  currentTime: number | undefined;
  canplay: boolean;
  error: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AudioService {
  private readonly className = 'AudioService';

  audioEvents = [
    'ended', 'error', 'play', 'playing', 'pause', 'timeupdate', 'canplay', 'loadedmetadata', 'loadstart'
  ];

  constructor(private logger: NGXLogger, private imagine: ImagineService) {}

  private stop$ = new Subject<void>();
  private audioObj = new Audio();

  private state: StreamState = {
    playing: false,
    readableCurrentTime: '',
    readableDuration: '',
    duration: undefined,
    currentTime: undefined,
    canplay: false,
    error: false,
  };

  private folderCache$: Observable<String[]>;
  private forceCacheReload$ = new Subject<void>();

  /** folders returns a cached observable with the current list of common prefixes in the bucket */
  get folders$(): Observable<String[]> {
    // This shareReplay operator returns an Observable that shares a single subscription
    // to the underlying source, which is the Observable returned from this.requestCountriesWithRegions()
    // https://blog.thoughtram.io/angular/2018/03/05/advanced-caching-with-rxjs.html
    if (! this.folderCache$) {
      this.logger.debug(`${this.className}.folders: first call to folder cache, retrieving from server`);
      this.folderCache$ = this.imagine.getEntityFolders(EntityType.Song)
        .pipe(
          takeUntil(this.forceCacheReload$),
          shareReplay(1), // Cache Size 1
        );
    }
    return this.folderCache$;
  }

  /** clearCaches calls next on reload$ subject will complete the current cache instance */
  clearCaches() {
    this.forceCacheReload$.next(); // causes folderCache$ Observable to complete subscription
    this.folderCache$ = null; // next call to folders$ will fill it again
    this.logger.debug(`${this.className}.clearCaches: All audio caches invalidated`);
  }

  private streamObservable(url) {
    return new Observable(observer => {
      // Play audio
      this.audioObj.src = url;
      this.audioObj.load();
      this.audioObj.play().then();

      const handler = (event: Event) => {
        this.updateStateEvents(event);
        observer.next(event);
      };

      this.addEvents(this.audioObj, this.audioEvents, handler);
      return () => {
        // Stop Playing
        this.audioObj.pause();
        this.audioObj.currentTime = 0;
        // remove event listeners & reset state
        this.removeEvents(this.audioObj, this.audioEvents, handler);
        this.resetState();
      };
    });
  }

  private addEvents(obj, events, handler) {
    events.forEach(event => {
      obj.addEventListener(event, handler);
    });
  }

  private removeEvents(obj, events, handler) {
    events.forEach(event => {
      obj.removeEventListener(event, handler);
    });
  }

  /** Plays the given URL */
  playStream(url) {
    return this.streamObservable(url).pipe(takeUntil(this.stop$));
  }

  play() {
    this.audioObj.play().then();
  }

  /** emits value in stop$ so playStream completes */
  stop() {
    this.stop$.next();
  }

  pause() {
    this.audioObj.pause();
  }

  /** Jumps to the given time */
  seekTo(seconds) {
    this.audioObj.currentTime = seconds;
  }

  /** Formats absolute settings to mm:ss format */
  formatTime(seconds: number /*, format: string = 'HH:mm:ss'*/) {
    // const momentTime = time * 1000;
    // return moment.utc(momentTime).format(format);
    // Convert seconds to HH-MM-SS with JavaScript?
    // https://stackoverflow.com/a/1322771/4292075
    // If SECONDS<3600 and if you want to show only MM:SS then use below code:
    return new Date(seconds * 1000).toISOString().substr(14, 5)
    // return format + time; // worry later
  }

  private stateChange: BehaviorSubject<StreamState> = new BehaviorSubject(this.state);

  /** Returns the current state of the Stream */
  getState(): Observable<StreamState> {
    return this.stateChange.asObservable();
  }

  private updateStateEvents(event: Event): void {
    switch (event.type) {
      case 'canplay':
        this.state.duration = this.audioObj.duration;
        this.state.readableDuration = this.formatTime(this.state.duration);
        this.state.canplay = true;
        break;
      case 'playing':
        this.state.playing = true;
        break;
      case 'pause':
        this.state.playing = false;
        break;
      case 'timeupdate':
        this.state.currentTime = this.audioObj.currentTime;
        this.state.readableCurrentTime = this.formatTime(this.state.currentTime);
        break;
      case 'error':
        this.resetState();
        this.state.error = true;
        break;
    }
    this.stateChange.next(this.state);
  }

  private resetState() {
    this.state = {
      playing: false,
      readableCurrentTime: '',
      readableDuration: '',
      duration: undefined,
      currentTime: undefined,
      canplay: false,
      error: false
    };
  }

}
