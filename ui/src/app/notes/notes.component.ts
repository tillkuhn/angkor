import { Component, OnInit } from '@angular/core';
import {Note} from '../domain/note';
import {ApiService} from '../api.service';
import {EnvironmentService} from '../environment.service';
import {NGXLogger} from 'ngx-logger';

@Component({
  selector: 'app-notes',
  templateUrl: './notes.component.html',
  styleUrls: ['./notes.component.sass']
})
export class NotesComponent implements OnInit {

  displayedColumns: string[] = ['notes', 'createdAt'];
  data: Note[] = [];
  isLoadingResults = true;

  constructor(private api: ApiService, private logger: NGXLogger) {
  }

  ngOnInit() {
    this.api.getNotes()
      .subscribe((res: any) => {
        this.data = res;
        this.logger.debug('getNotes()', this.data);
        this.isLoadingResults = false;
      }, err => {
        this.logger.error(err);
        this.isLoadingResults = false;
      });
  }
}
