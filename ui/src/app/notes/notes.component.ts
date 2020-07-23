import { Component, OnInit } from '@angular/core';
import {Note} from '../domain/note';
import {ApiService} from '../api.service';
import {EnvironmentService} from '../environment.service';
import {NGXLogger} from 'ngx-logger';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-notes',
  templateUrl: './notes.component.html',
  styleUrls: ['./notes.component.scss']
})
export class NotesComponent implements OnInit {

  displayedColumns: string[] = ['summary', 'status','createdAt'];
  data: Note[] = [];
  newItemForm: FormGroup;
  summary = '';
  isLoadingResults = true;

  constructor(private api: ApiService, private logger: NGXLogger,private formBuilder: FormBuilder,private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.newItemForm = this.formBuilder.group({
      summary : [null, Validators.required]
    });
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

  onFormSubmit() {
    this.isLoadingResults = true;
    this.api.addNote(this.newItemForm.value)
      .subscribe((res: any) => {
        const id = res.id;
        this.isLoadingResults = false;
        this.snackBar.open('Quicknote saved with id '+id, 'Close', {
          duration: 2000,
        });
        this.ngOnInit(); // reset / reload list
        // this.router.navigate(['/place-details', id]);
      }, (err: any) => {
        this.logger.error(err);
        this.isLoadingResults = false;
      });
  }
}
