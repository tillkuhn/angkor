import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DishAddComponent} from './dish-add.component';

describe('DishAddComponent', () => {
  let component: DishAddComponent;
  let fixture: ComponentFixture<DishAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DishAddComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DishAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
