import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FileUploadComponent} from './file-upload.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatIconModule} from '@angular/material/icon';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {ClipboardModule} from '@angular/cdk/clipboard';
import {MatDialogModule} from '@angular/material/dialog';
import {EntityType} from '../../../domain/entities';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatTableModule} from '@angular/material/table';
import {RouterTestingModule} from '@angular/router/testing';
import {BytesizePipe} from '../../../pipes/bytesize.pipe';
import {WebStorageModule} from 'ngx-web-storage';

fdescribe('FileUploadComponent', () => {
  let component: FileUploadComponent;
  let fixture: ComponentFixture<FileUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
    declarations: [FileUploadComponent, BytesizePipe],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    imports: [HttpClientTestingModule, FormsModule, ReactiveFormsModule, MatSnackBarModule, WebStorageModule,
        MatIconModule, LoggerTestingModule, ClipboardModule, MatDialogModule, MatTableModule, RouterTestingModule],
    teardown: { destroyAfterEach: false }
})
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FileUploadComponent);
    component = fixture.componentInstance;
    component.entityType = EntityType.Place; // does not work :-(
    fixture.detectChanges();
  });

  it('should create', () => {
    // Init values or we can't call onInit() since entityType is set via @Input
    // https://codecraft.tv/courses/angular/unit-testing/components/
    // component.entityType = 'PLACE'; // does not work :-(
    // fixture.detectChanges();
    expect(component).toBeTruthy();
  });
});
