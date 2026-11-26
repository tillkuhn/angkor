import {Component, ChangeDetectionStrategy} from '@angular/core';
import {PhotoStoreService} from '@app/locatables/photos/photo-store.service';

@Component({
    selector: 'app-photo-details',
    templateUrl: './photo-details.component.html',
    styleUrls: [],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class PhotoDetailsComponent {

  constructor(
    public store: PhotoStoreService,
  ){}

}
