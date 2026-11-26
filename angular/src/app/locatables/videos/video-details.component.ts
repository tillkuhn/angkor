import {Component, ChangeDetectionStrategy} from '@angular/core';
import {VideoStoreService} from '@app/locatables/videos/video-store.service';

@Component({
    selector: 'app-video-details',
    templateUrl: './video-details.component.html',
    styleUrls: [],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class VideoDetailsComponent {

  constructor(
    public store: VideoStoreService,
  ) {}

}
