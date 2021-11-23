import {Component} from '@angular/core';
import {PhotoStoreService} from '@app/locations/photos/photo-store.service';

@Component({
  selector: 'app-photo-details',
  templateUrl: './photo-details.component.html',
  styleUrls: []
})
export class PhotoDetailsComponent {

  constructor(
    public store: PhotoStoreService,
  ){}

}
