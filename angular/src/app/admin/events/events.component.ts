import {Component, OnInit} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {catchError, map, tap} from 'rxjs/operators';
import {ApiHelper} from '@shared/helpers/api-helper';
import {HttpClient} from '@angular/common/http';
import {EntityEventService} from '@shared/services/entity-event.service';
import {ApiEvent, Event} from '@domain/event';

@Component({
  selector: 'app-events',
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.scss']
})
export class EventsComponent implements OnInit {

  data: Event[] = [];
  displayedColumns: string[] = ['topic', 'action', 'message', 'time'];
  dateOptions: {
    // locale: es,
    addSuffix: true
  };

  constructor(private http: HttpClient,
              private logger: NGXLogger,
              private eventService: EntityEventService,
  ) {
  }

  ngOnInit(): void {
    this.getEvents().subscribe(data => {
      this.data = data;
    });
  }

  getEvents(): Observable<Event[]> {
    return this.http.get<ApiEvent[]>(`${environment.apiUrlRoot}/admin/events`)
      .pipe(
        map<ApiEvent[], Event[]>(items =>
          items.map(item => this.mapFromApiEntity(item)),
        ),
        tap(events => this.logger.debug(`svc fetched ${events.length} metrics`)),
        catchError(ApiHelper.handleError('getEvents', this.eventService, []))
      );
  }

  // override standard mapper in superclass
  mapFromApiEntity(apiEntity: ApiEvent): Event {
    return {
      ...apiEntity,
      time: ApiHelper.parseISO(apiEntity.time),
    };
  }

}
