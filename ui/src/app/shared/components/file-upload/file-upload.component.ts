import {Component, EventEmitter, Inject, Input, OnInit, Output} from '@angular/core';
import {HttpEventType, HttpResponse} from '@angular/common/http';
import {FileService} from '../../file.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {EntityType} from '../../../domain/entities';
import {NGXLogger} from 'ngx-logger';
import {FileItem, FileUpload} from '../../../domain/file-item';
import {timer} from 'rxjs';
import {Clipboard} from '@angular/cdk/clipboard';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';

const REFRESH_AFTER_UPLOAD_DELAY_MS = 2000;

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit {

  @Input() entityId: string;
  @Input() entityType: string;
  @Input() enableUpload: boolean;
  @Input() enableDelete: boolean;
  // https://fireship.io/lessons/sharing-data-between-angular-components-four-methods/
  @Output() imageEvent = new EventEmitter<string>();

  fileColumns: string[] = ['filename'/*, 'tags'*/];
  files: FileItem[] = [];
  currentFileUpload: File;
  progressInfo: string;
  progress: { percentage: number } = {percentage: 0};

  constructor(private fileService: FileService,
              private logger: NGXLogger,
              private snackBar: MatSnackBar,
              private clipboard: Clipboard,
              private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.loadFiles();
  }

  // https://medium.com/@altissiana/how-to-pass-a-function-to-a-child-component-in-angular-719fc3d1ee90
  loadFiles() {
    if (! this.entityType ) {
      this.logger.warn('Warn entityType is empty');
      // return; // should throw
    }

    this.progressInfo = 'refreshing filelist';
    this.fileService.getEntityFiles(EntityType[this.entityType], this.entityId)
      .subscribe((res: FileItem[]) => {
        this.files = res;
        this.logger.debug('FileUploadComponent.getFiles', this.files.length);
        this.progressInfo = `${this.files.length} items found`;
      }, err => {
        this.logger.error(err);
      });
  }

  // this one gets triggered by the upload button
  onFileChangUpload(event) {
    // this.progress.percentage = 0;
    this.currentFileUpload = event.target.files[0]; // this.selectedFiles.item(0);
    this.logger.info(`Uploading ${this.currentFileUpload} to server`);
    this.fileService.uploadFile(this.currentFileUpload, EntityType[this.entityType], this.entityId).subscribe(event => {
        if (event.type === HttpEventType.UploadProgress) {
          this.progress.percentage = Math.round(100 * event.loaded / event.total);
        } else if (event instanceof HttpResponse) {
          const body = (event as HttpResponse<any>).body;
          this.logger.debug('File Successfully uploaded', body);
          const refreshTimer = timer(REFRESH_AFTER_UPLOAD_DELAY_MS);
          // s3 upload is async, so we trigger a list reload after a reasonable waiting period
          refreshTimer.subscribe(val => {
            this.logger.debug(`trigger file list reload ${val}`);
            this.loadFiles();
          });
          this.snackBar.open(`File Successfully uploaded: ${body}`, 'Close');
        }
        this.currentFileUpload = undefined;
      }
    );
  }

  // copies path to the clipboard so it can be pasted to another input elememnt
  setImageAsTitle(path) {
    // this.logger.debug('copy path to clipboard',path);
    const fullpath = path + '?large';
    this.clipboard.copy(fullpath);
    this.imageEvent.emit(fullpath);
    // this.snackBar.open(`${fullpath} copied to clipboard`, 'Close');
  }

  openFileInputDialog(): void {
    const dialogRef = this.dialog.open(FileInputDialogComponent, {
      width: '350px',
      data: {entityType: this.entityType, entityId: this.entityId}
    });

    dialogRef.afterClosed().subscribe(dialogResponse => {
      this.logger.debug(`Dialog was closed result ${dialogResponse}`);
      if (dialogResponse && dialogResponse.url) {
        this.fileService.uploadUrl(dialogResponse, EntityType[this.entityType], this.entityId).subscribe(event => {
          this.logger.debug('Request for URL queued', event);
          this.snackBar.open('Request for URL queued:' + JSON.stringify(event), 'Close');
          // s3 upload is async, so we trigger a list reload after a reasonable waiting period
          // wait longer since we're not sure how long the download takes
          timer(REFRESH_AFTER_UPLOAD_DELAY_MS * 2).subscribe(val => {
            this.logger.debug(`trigger file list reload ${val}`);
            this.loadFiles();
          });
        });
      } else {
        this.snackBar.open(`Invalid URL, sorry`, 'Close');
      }
      // this.animal = result;
    });
  }

}

@Component({
  selector: 'app-file-input',
  templateUrl: 'file-input-dialog.component.html',
})
export class FileInputDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<FileInputDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: FileUpload) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
