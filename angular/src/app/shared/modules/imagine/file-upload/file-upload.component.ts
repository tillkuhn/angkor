import {Component, EventEmitter, Inject, Input, OnInit, Output} from '@angular/core';
import {HttpEventType, HttpResponse} from '@angular/common/http';
import {ImagineService} from '@shared/modules/imagine/imagine.service';
import {MatLegacySnackBar as MatSnackBar} from '@angular/material/legacy-snack-bar';
import {EntityType} from '@shared/domain/entities';
import {NGXLogger} from 'ngx-logger';
import {FileItem, FileUpload} from '../file-item';
import {timer} from 'rxjs';
import {Clipboard} from '@angular/cdk/clipboard';
import {MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialog as MatDialog, MatLegacyDialogRef as MatDialogRef} from '@angular/material/legacy-dialog';
import {FileEvent} from '@shared/modules/imagine/file-event';

// time to wait for the average upload to trigger auto reload of file list
const REFRESH_AFTER_UPLOAD_DELAY_MS = 2000;

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit {

  private readonly className = 'ImagineUpload';

  @Input() entityId: string;
  @Input() entityType: string;
  @Input() enableUpload: boolean;
  @Input() enableDelete: boolean;
  /** Emit Events, see https://fireship.io/lessons/sharing-data-between-angular-components-four-methods/ */
  @Output() imagineEvent = new EventEmitter<FileEvent>();

  fileColumns: string[] = ['filename'/*, 'tags'*/];
  files: FileItem[] = [];
  currentFileUpload: File;
  progressInfo: string;
  progress: { percentage: number } = {percentage: 0};

  constructor(private fileService: ImagineService,
              private logger: NGXLogger,
              private snackBar: MatSnackBar,
              private clipboard: Clipboard,
              private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.loadFiles();
  }

  /** Load initial list of file items, if any are available in the depths for the cloud */
  loadFiles() {
    if (!this.entityType) {
      const msg = 'Warn entityType is empty';
      this.logger.error(msg);
      throw Error(msg);
    }

    this.progressInfo = 'refreshing fileList';
    this.fileService.getEntityFiles(EntityType[this.entityType], this.entityId)
      .subscribe({
        next: (res: FileItem[]) => {
          this.files = res;
          this.logger.debug(`${this.className}.loadFiles: ${this.files ? this.files.length : 0}`);
          this.progressInfo = this.files ? `${this.files.length} items found` : 'no files';
          this.imagineEvent.next({subject: 'LIST_REFRESH', data: this.files});
        },
        error: err => {
          this.logger.error(`${this.className}.loadFiles: error during ${this.progressInfo} ${err}`);
        }
      });
  }

  // this one gets triggered by the upload button
  onFileChangeUpload(viewEvent) {
    this.currentFileUpload = viewEvent.target.files[0]; // this.selectedFiles.item(0);
    this.logger.info(`${this.className}.onFileChangUpload: Uploading ${this.currentFileUpload?.name} to server`);
    this.fileService.uploadFile(this.currentFileUpload, EntityType[this.entityType], this.entityId).subscribe(event => {
        if (event.type === HttpEventType.UploadProgress) {
          this.progress.percentage = Math.round(100 * event.loaded / event.total);
        } else if (event instanceof HttpResponse) {
          const body = (event as HttpResponse<any>).body;
          this.logger.debug(`${this.className}.onFileChangUpload: File Successfully uploaded ${body}`);

          // s3 upload is async, so we trigger a list reload after a reasonable waiting period
          const refreshTimer = timer(REFRESH_AFTER_UPLOAD_DELAY_MS);
          refreshTimer.subscribe(_ => {
            this.logger.debug(`${this.className}.onFileChangUpload: Triggered file list reload after ${REFRESH_AFTER_UPLOAD_DELAY_MS}`);
            this.loadFiles();
          });
          this.snackBar.open(`File Successfully uploaded: ${body}`, 'Close');

          // Emit Acknowledge Event, body will look as follows:
          // {"requestId":"c7m4ktc6f0.....","origin":"multipart/form-data","filename":"bowl120220119_224217_bowl_mini.jpg","key":"imagine/places/e383f68f-bf97-466e-bf4e-d7bd529e23a5/bowl120220119_224217_bowl_mini.jpg","size":243672}
          this.imagineEvent.next({subject: 'ACKNOWLEDGED', data: body});
        }
        this.currentFileUpload = undefined;
      }
    );
  }

  // copies path to the clipboard, so it can be pasted to another input element
  setImageAsTitle(path) {
    // this.logger.debug('copy path to clipboard',path);
    this.clipboard.copy(path);
    this.imagineEvent.emit({data: path, subject: 'SELECTED'});
  }

  isImage(item: FileItem): boolean {
    // this.logger.info(item.tags.ContentType);
    return item.tags.ContentType?.indexOf('image/') > -1;
  }

  isPDF(item: FileItem): boolean {
    return item.tags.ContentType === 'application/pdf';
  }

  openFileInputDialog(): void {
    const dialogRef = this.dialog.open(FileInputDialogComponent, {
      width: '350px',
      data: {entityType: this.entityType, entityId: this.entityId}
    });

    dialogRef.afterClosed().subscribe(dialogResponse => {
      this.logger.debug(`${this.className}: Dialog was closed, result=${dialogResponse}`);
      if (dialogResponse && dialogResponse.url) {
        this.fileService.uploadUrl(dialogResponse, EntityType[this.entityType], this.entityId).subscribe(event => {
          this.logger.debug(`${this.className}.openFileInputDialog: Request for URL queued  ${event}`);
          this.snackBar.open('Request for URL queued:' + JSON.stringify(event), 'Close');
          // s3 upload is async, so we trigger a list reload after a reasonable waiting period
          // wait longer since we're not sure how long the download takes
          timer(REFRESH_AFTER_UPLOAD_DELAY_MS * 2).subscribe(val => {
            this.logger.debug(`${this.className}.openFileInputDialog: trigger file list reload ${val}`);
            this.loadFiles();
          });
        });
      } else {
        this.snackBar.open(`Invalid URL, sorry`, 'Close');
      }
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
