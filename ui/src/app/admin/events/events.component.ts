import {Component, OnInit} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {catchError, tap} from 'rxjs/operators';
import {ApiHelper} from '@shared/helpers/api-helper';
import {HttpClient} from '@angular/common/http';
import {EntityEventService} from '@shared/services/entity-event.service';

@Component({
  selector: 'app-events',
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.scss']
})
export class EventsComponent implements OnInit {

  data: Event[] = [];
  displayedColumns: string[] = ['partition', 'topic', 'action', 'message', 'source'];

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
    return this.http.get<Event[]>(`${environment.apiUrlRoot}/admin/events`)
      .pipe(
        tap(events => this.logger.debug(`svc fetched ${events.length} metrics`)),
        catchError(ApiHelper.handleError('getEvents', this.eventService, []))
      );
  }
}
