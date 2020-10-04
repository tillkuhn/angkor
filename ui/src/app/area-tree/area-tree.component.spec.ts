import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AreaTreeComponent } from './area-tree.component';
import {RouterTestingModule} from '@angular/router/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('AreaTreeComponent', () => {
  let component: AreaTreeComponent;
  let fixture: ComponentFixture<AreaTreeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AreaTreeComponent ],
      imports: [RouterTestingModule, LoggerTestingModule, HttpClientTestingModule]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AreaTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
