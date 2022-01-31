import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NGXLogger} from 'ngx-logger';


export declare type StarRatingColor = 'primary' | 'accent' | 'warn';

// Based on
// https://github.com/ERS-HCL/star-rating-angular-material/tree/master/star-rating
// and
// https://github.com/hughjdavey/ngx-stars/tree/master/src/lib
@Component({
  selector: 'app-rating',
  templateUrl: './rating.component.html',
  styleUrls: ['./rating.component.scss']
})
export class RatingComponent implements OnInit {

  private readonly className = 'RatingComponent';

  @Input() initialRating: number = 0;
  @Input() starCount: number = 5;
  @Input() color: StarRatingColor = 'accent';
  @Input() readonly: boolean = false;

  @Output() ratingUpdated = new EventEmitter<number>();

  rating: number = 0;
  ratingArr: number[] = [];

  constructor(
    private logger: NGXLogger,
  ) {}

  // parent may have to use *ngIf to make sure the value passed to e.g. initialRating is already initialized
  // or ngOnInit will be called with default values, not the ones passed in. see e.g. dish-edit.component.html
  ngOnInit(): void {
    const rounded = Math.round(this.initialRating)
    this.logger.trace(`${this.className} init rating with ${rounded}/${this.starCount} (exact: ${this.initialRating})`)
    this.rating = rounded
    this.ratingArr = []
    for (let index = 0; index < this.starCount; index++) {
      this.ratingArr.push(index);
    }
  }

  onClick(newRating: number) {
    if (! this.readonly) {
      this.logger.debug(`${this.className}: Set Rating to ${newRating} readonly=${this.readonly}`);
      this.rating = newRating;
      this.ratingUpdated.emit(newRating);
    } else {
      this.logger.warn(`${this.className}: Ignore set Rating to ${newRating}, component is readonly`);
    }
    return false;
  }

  /** returns the value for mat-icon depending on whether the star is filled (within rating range) or not  */
  showIcon(index: number) {
    if (this.rating >= index + 1) {
      return 'star';
    } else {
      return 'star_border';
    }
  }

}
