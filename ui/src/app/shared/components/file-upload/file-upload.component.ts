import {Component, Input, OnInit} from '@angular/core';
import {HttpEventType, HttpResponse} from '@angular/common/http';
import {FileService} from '../../file.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {EntityType} from '../../../domain/common';
import {NGXLogger} from 'ngx-logger';
import {FileItem} from '../../../domain/file-item';
import {timer} from 'rxjs';
import { Clipboard } from '@angular/cdk/clipboard';

const REFRESH_AFTER_UPLOAD_DELAY_MS = 2000;
@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit {

  @Input() entityId: string;
  @Input() entityType: string;
  // https://medium.com/@altissiana/how-to-pass-a-function-to-a-child-component-in-angular-719fc3d1ee90
  // @Input() refreshCallback: (args: any) => void;
  // https://medium.com/@altissiana/how-to-pass-a-function-to-a-child-component-in-angular-719fc3d1ee90
  // refreshCallback = (args: any): void => {
  //   this.loadFiles();
  // }
  //

  fileColumns: string[] = ['filename', 'tags'];
  files: FileItem[] = [];
  progressInfo: string;

  // Todo cleanup the blueprint contained much more than we need
  selectedFiles: FileList;
  currentFileUpload: File;
  progress: { percentage: number } = {percentage: 0};
  selectedFile = null;
  changeImage = false;

  constructor(private fileService: FileService,
              private logger: NGXLogger,
              private snackBar: MatSnackBar,
              private clipboard: Clipboard) {
  }

  ngOnInit(): void {
    this.loadFiles();
  }

  change($event) {
    this.changeImage = true;
  }

  changedImage(event) {
    this.selectedFile = event.target.files[0];
  }

  // https://medium.com/@altissiana/how-to-pass-a-function-to-a-child-component-in-angular-719fc3d1ee90
  loadFiles() {
    this.progressInfo = 'refreshing filelist';
    this.fileService.getEntityFiles(EntityType[this.entityType], this.entityId)
      .subscribe((res: FileItem[]) => {
        this.files = res;
        this.logger.debug('getFiles()', this.files.length);
        this.progressInfo = `${this.files.length} items found`;
      }, err => {
        this.logger.error(err);
      });
  }

  // this one gets triggered by the upload button
  upload() {
    this.progress.percentage = 0;
    this.currentFileUpload = this.selectedFiles.item(0);
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
        this.selectedFiles = undefined;
      }
    );
  }

  copyImagePath(path) {
    // this.logger.debug('copy path to clipboard',path);
    const fullpath = path + '?large';
    this.clipboard.copy(fullpath);
    this.snackBar.open(`${fullpath} copied to clipboard`, 'Close');
  }

  selectFile(event) {
    this.selectedFiles = event.target.files;
  }

  /*
downloadFile() {
  const link = document.createElement('a');
  link.setAttribute('target', '_blank');
  link.setAttribute('href', '_File_Saved_Path');
  link.setAttribute('download', 'file_name.pdf');
  document.body.appendChild(link);
  link.click();
  link.remove();
}
 */

}
