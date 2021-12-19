import {Component, OnInit} from '@angular/core';
import {ImagineService} from '@shared/modules/imagine/imagine.service';
import {EntityType} from '@shared/domain/entities';
import {FileItem} from '@shared/modules/imagine/file-item';
import {NGXLogger} from 'ngx-logger';

@Component({
  selector: 'app-song',
  templateUrl: './radio.component.html',
  styleUrls: ['./radio.component.scss']
})
export class RadioComponent implements OnInit {

  private readonly className = 'RadioComponent';

  songs: FileItem[] = [];

  constructor(private imagineService: ImagineService, private logger: NGXLogger,) {
  }

  ngOnInit(): void {
    this.imagineService.getEntityFiles(EntityType.Song).subscribe(res => {
      this.songs = res;
      this.logger.debug(`${this.className}.loadFiles: ${this.songs ? this.songs.length : 0}`);
    });
  }

  playSong(song: FileItem) {
    this.logger.debug(`Play song ${song.path}`)
    this.imagineService.getPresignUrl(song.path).subscribe( r => this.logger.info(`result ${r}`))
  }

// {
//   "filename": "imagine/songs/01 - Mein Lied.mp3",
//   "path": "/imagine/songs/01 - Mein Lied.mp3",
//   "tags": {
//     "Album": "Fascination Space",
//     "Artist": "Drive in Vacation Racing Team",
//     "ContentType": "audio/mpeg",
//     "Genre": "Rock",
//     "Origin": "multipart/form-data",
//     "Size": "5743463",
//     "Title": "Mein Lied",
//     "Track": "1/15",
//     "Year": "2014"
//   }

}
