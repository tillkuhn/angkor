import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
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

  @Input('initialRating') initialRating: number;
  @Input('starCount') starCount: number;
  @Input('color') color: StarRatingColor;
  @Input() readonly: boolean;

  @Output() ratingUpdated = new EventEmitter();

  rating: number = 0;
  snackBarDuration: number = 2000;
  ratingArr = [];

  constructor(
    private logger: NGXLogger,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit() {
    this.rating = this.initialRating
    for (let index = 0; index < this.starCount; index++) {
      this.ratingArr.push(index);
    }
  }

  onClick(rating: number) {
    this.logger.debug(`Set Rating to ${rating} readonly=${this.readonly}`);
    this.snackBar.open('You rated ' + rating + ' / ' + this.starCount, '', {
      duration: this.snackBarDuration
    });
    this.rating = rating;
    this.ratingUpdated.emit(rating);
    return false;
  }

  showIcon(index: number) {
    if (this.rating >= index + 1) {
      return 'star';
    } else {
      return 'star_border';
    }
  }

}
