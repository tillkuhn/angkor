import {AfterViewInit, Component, ElementRef, Input, OnChanges, SimpleChanges, ViewChild} from '@angular/core';

// Thank you https://codesandbox.io/s/3j3mq7ykp?from-embed=&file=/src/animated/animated-digit.component.ts
@Component({
  selector: 'app-count-up',
  templateUrl: './count-up.component.html',
  styleUrls: ['./count-up.component.scss']
})
export class CountUpComponent implements AfterViewInit, OnChanges {
  // Style props https://stackoverflow.com/a/43903570/4292075
  @Input() height = '60px';
  @Input() width = '60px';
  @Input() borderRadius = '60px';
  @Input() fontSize = '32pt';
  @Input() padding = '24px';
  @Input() color = 'white';

  // Input for actual counter
  @Input() duration: number;
  @Input() digit: number;
  @Input() steps: number;
  @ViewChild('animatedDigit') animatedDigit: ElementRef;


  ngAfterViewInit() {
    if (this.digit && this.animatedDigit) {
      this.animateCount();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.digit && this.animatedDigit) {
      this.animateCount();
    }
  }

  animateCount() {
    if (!this.duration) {
      this.duration = 1000;
    }
    this.counterFunc(this.digit, this.duration, this.animatedDigit);
    /*
    if (typeof this.digit === 'number') {
      if (this.animatedDigit ) {
        console.log('INFO: Starting counterFunc for animatedDigit');
        this.counterFunc(this.digit, this.duration, this.animatedDigit);
      } else {
        console.log('WARNING: animatedDigit is undefined');
      }
    }
     */
  }

  counterFunc(endValue, durationMs, element): void {

    if (!this.steps) {
      this.steps = 12;
    }

    const stepCount = Math.abs(durationMs / this.steps);
    const valueIncrement = (endValue - 0) / stepCount;
    const sinValueIncrement = Math.PI / stepCount;

    let currentValue = 0;
    let currentSinValue = 0;

    function step() {
      currentSinValue += sinValueIncrement;
      currentValue += valueIncrement * Math.sin(currentSinValue) ** 2 * 2;

      element.nativeElement.textContent = Math.abs(Math.floor(currentValue));

      if (currentSinValue < Math.PI) {
        window.requestAnimationFrame(step);
      }
    }

    step();
  }

}
