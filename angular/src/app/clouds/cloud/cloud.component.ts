import {Component, OnInit} from '@angular/core';
import {CloudData, CloudOptions} from 'angular-tag-cloud-module';
import {TagService} from '@shared/components/tag-input/tag.service';
import {NGXLogger} from 'ngx-logger';
import {EntityType} from '@shared/domain/entities';
import {map} from 'rxjs/operators';
import {TagSummary} from '@domain/tag';

@Component({
  selector: 'app-cloud',
  templateUrl: './cloud.component.html'
})
export class CloudComponent implements OnInit {

  // from https://github.com/mrmrs/colors
  colors = ['#001F3F', '#0074D9', '#7FDBFF', '#39CCCC', '#3D9970', '#2ECC40', '#01FF70'];

  options: CloudOptions = {
    // if width is between 0 and 1 it will be set to the width of the upper element multiplied by the value
    width: 1000,
    // if height is between 0 and 1 it will be set to the height of the upper element multiplied by the value
    height: 400,
    overflow: false,
  };

  placeData: CloudData[] = [];
  dishData: CloudData[] = [];

  constructor(private tagService: TagService, private logger: NGXLogger) {
  }

  ngOnInit(): void {
    const operation = 'CloudComponent';
    this.logger.debug(`${operation}.ngOnInit`);
    //  {text: 'Weight-8-link-color', weight: 8, link: 'https://google.com', color: '#ffaaee'}
    this.tagService.entityTagsRaw(EntityType.Place)
      .pipe(
        map<TagSummary[], CloudData[]>(tagSummaries =>
          tagSummaries.map(tagSummary => {
            return {text: tagSummary.label, weight: tagSummary.count, color: this.randomColor()};
          })
        )
      ).subscribe(cloudData => this.placeData = cloudData);
    this.tagService.entityTagsRaw(EntityType.Dish)
      .pipe(
        map<TagSummary[], CloudData[]>(tagSummaries =>
          tagSummaries.map(tagSummary => {
            return {text: tagSummary.label, weight: tagSummary.count, color: this.randomColor()};
          })
        )
      ).subscribe(cloudData => this.dishData = cloudData);
  }

  randomColor() {
    return this.colors[Math.floor(Math.random() * this.colors.length)];
  }
}
