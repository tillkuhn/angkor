import {ComponentFixture, TestBed} from '@angular/core/testing';

import {LocationSearchComponent} from './location-search.component';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {MatCardModule} from '@angular/material/card';
import {LayoutModule} from '@angular/cdk/layout';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatTabsModule} from '@angular/material/tabs';
import {MatTableModule} from '@angular/material/table';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatInputModule} from '@angular/material/input';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatIconModule} from '@angular/material/icon';
import {NgxWebstorageModule} from 'ngx-webstorage';
import {MatMenuModule} from '@angular/material/menu';
import {EntityType} from '@shared/domain/entities';
import {ActivatedRoute} from '@angular/router';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
// Angular15
import {MatDialogModule} from '@angular/material/dialog';

describe('ToursComponent', () => {
  let component: LocationSearchComponent;
  let fixture: ComponentFixture<LocationSearchComponent>;

  const location = {
    id: '42d49bb1-4658-4dc3-89e3-29a527f1aaaa',
    externalId: '12345abc',
    name: 'Monastery Old Mill',
    imageUrl: 'https://img.youtube.com/vi/12345abc/default.jpg',
    primaryUrl: 'https://youtu.be/12345abc',
    authScope: 'PUBLIC',
    coordinates: [11.0515238, 39.5462771],
    tags: [],
    properties: {},
    createdAt: '2021-09-26T22:35:26.754155Z',
    createdBy: '39134950-97ef-4961-a4b1-96b1bacc8b9c',
    updatedAt: '2021-10-29T15:23:39.587143Z',
    updatedBy: '00000000-0000-0000-0000-000000000001',
    entityType: EntityType.Video
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [LocationSearchComponent, {
        provide: ActivatedRoute,
        useValue: {
          snapshot: {
            data: {entityType: EntityType.Tour}
          },
        }
      }
      ],
      declarations: [LocationSearchComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [BrowserAnimationsModule,
        FormsModule,
        HttpClientTestingModule,
        LayoutModule,
        LoggerTestingModule,
        MatCardModule,
        MatDialogModule,
        MatFormFieldModule,
        MatIconModule,
        MatIconTestingModule,
        MatInputModule,
        MatMenuModule,
        MatSelectModule,
        MatSnackBarModule,
        MatTableModule,
        MatTabsModule,
        ReactiveFormsModule,
        RouterTestingModule,
        NgxWebstorageModule.forRoot()],
      teardown: {destroyAfterEach: false}
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationSearchComponent);

    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return smaller image for youtube images', () => {
    expect(component.previewImageUrl(location)).toBe('https://img.youtube.com/vi/12345abc/default.jpg');
    location.imageUrl = '';
    expect(component.previewImageUrl(location)).toBe('/assets/icons/video.svg');
    location.imageUrl = '/images/hase.jpg';
    expect(component.previewImageUrl(location)).toBe('/images/hase.jpg');
  });

  /** verify whether an entity type support add or not*/
  it('should verify if add details is supported', () => {
    component.entityType = EntityType.Place;
    expect(component.addDetailsSupported()).toBeTruthy();
    component.entityType = EntityType.Video;
    expect(component.addDetailsSupported()).toBeTruthy();
    component.entityType = EntityType.Post;
    expect(component.addDetailsSupported()).toBeFalsy();
  });

});
