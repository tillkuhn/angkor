import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {LinkStoreService} from '@app/links/link-store.service';
import {AuthService} from '@shared/services/auth.service';
import {NGXLogger} from 'ngx-logger';
import {Link} from '@domain/link';

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.scss', '../../shared/components/common.component.scss'],
  encapsulation: ViewEncapsulation.None // https://stackoverflow.com/a/56978906/4292075 to overwrite padding for list item

})
export class FeedComponent implements OnInit {

  feeds: Link[];
  feed: any;
  rating: 0;

  constructor(public linkService: LinkStoreService,
              public authService: AuthService,
              private logger: NGXLogger) {
  }

  ngOnInit(): void {
    this.logger.debug(`FeedComponent.onInit`);
    this.linkService.getFeed$().subscribe(feedList => this.feeds = feedList);
  }

  displayFeed(event: any) {
    const id = event.value;
    this.logger.debug(`display feed ${id}`); // value == id
    this.linkService.getFeed(id).subscribe(feed => this.feed = feed);
  }

}
