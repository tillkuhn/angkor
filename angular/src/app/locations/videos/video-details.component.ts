import {Component} from '@angular/core';
import {VideoStoreService} from '@app/locations/videos/video-store.service';

@Component({
  selector: 'app-video-details',
  templateUrl: './video-details.component.html',
  styleUrls: []
})
export class VideoDetailsComponent {

  constructor(
    public store: VideoStoreService,
  ) {}

}
