import {Component} from '@angular/core';
import {PostStoreService} from '@app/locatables/posts/post-store.service';

@Component({
  selector: 'app-post-details',
  templateUrl: './post-details.component.html',
  styleUrls: []
})
export class PostDetailsComponent {

  constructor(
    public store: PostStoreService,
  ) {}

}
