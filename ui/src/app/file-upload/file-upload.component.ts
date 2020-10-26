import {Component, OnInit} from '@angular/core';
import {HttpResponse, HttpEventType} from '@angular/common/http';
import {FileService} from '../shared/file.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit {

  selectedFiles: FileList;
  currentFileUpload: File;
  progress: { percentage: number } = {percentage: 0};
  selectedFile = null;
  changeImage = false;

  constructor(private fileService: FileService,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
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

  change($event) {
    this.changeImage = true;
  }

  changedImage(event) {
    this.selectedFile = event.target.files[0];
  }

  upload() {
    this.progress.percentage = 0;
    this.currentFileUpload = this.selectedFiles.item(0);
    this.fileService.uploadFile(this.currentFileUpload,'1234').subscribe(event => {
        if (event.type === HttpEventType.UploadProgress) {
          this.progress.percentage = Math.round(100 * event.loaded / event.total);
        } else if (event instanceof HttpResponse) {
          const body = (event as HttpResponse<any>).body;
          this.snackBar.open(`File Successfully uploaded: ${body}`, 'Close');
        }
        this.selectedFiles = undefined;
      }
    );
  }

  selectFile(event) {
    this.selectedFiles = event.target.files;
  }

}
