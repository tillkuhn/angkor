import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CountUpComponent} from './count-up.component';

describe('CountUpComponent', () => {
  let component: CountUpComponent;
  let fixture: ComponentFixture<CountUpComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CountUpComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CountUpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
