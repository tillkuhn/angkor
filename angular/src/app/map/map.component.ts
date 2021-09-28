// Mapbox and Geo Imports we need to import the "official" MapComponent as an alias
// since we foolishly called also our own class "MapComponent" :-)
import MapboxGeocoder from '@mapbox/mapbox-gl-geocoder';
import {ActivatedRoute} from '@angular/router';
import {AreaStoreService} from '@app/areas/area-store.service';
import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {EnvironmentService} from '@shared/services/environment.service';
import {Feature, Point} from 'geojson';
import {LinkStoreService} from '@app/links/link-store.service';
import {MapComponent as MapboxGLMapComponent} from 'ngx-mapbox-gl';
import {MapboxGeoJSONFeature, MapLayerMouseEvent} from 'mapbox-gl';
import {MasterDataService} from '@shared/services/master-data.service';
import {NGXLogger} from 'ngx-logger';
import {POI} from '@domain/poi';
import {REGEXP_COORDINATES} from '@shared/domain/smart-coordinates';
import {environment} from '../../environments/environment';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit /* AfterViewInit */ {

  // Constants for selected Zoom Levels
  // 10 ~ detailed like bangkok + area, 5 ~ southeast asia, 0 ~ the earth
  // More Details: https://docs.mapbox.com/help/glossary/zoom-level/
  static readonly DEEPLINK_POI_ZOOM = 12; // if called with maps/@lat,lon
  static readonly ON_CLICK_POI_ZOOM = 6; // if poi is clicked
  static readonly DEFAULT_POI_ZOOM = 2; // default when /map is launched w/o args

  private readonly className = 'MapComponent';
  private locationType2Maki: Map<string, string> = new Map();

  // Obtain access to the Map Component
  // https://angular-2-training-book.rangle.io/advanced-components/access_child_components
  @ViewChild(MapboxGLMapComponent) mapbox: MapboxGLMapComponent;

  // mapStyles is an array of different map styles like outdoor, satellite to puck from
  // Check https://docs.mapbox.com/mapbox-gl-js/example/setstyle/ for code you to set via API
  // For alternative styles, streets-v11, check https://docs.mapbox.com/api/maps/#styles
  // Remove (longer needed): {description: 'Street',id: 'streets-v11'}
  readonly mapStyles = [
    {
      description: 'Outdoor',
      id: 'outdoors-v11'
    },
    {
      description: 'Satellite',
      id: 'satellite-streets-v11' // 'satellite-v9' is w/o streets
    }
  ];

  // This holds the current style and is bound via [style] on the ngl-map element
  mapStyle = `mapbox://styles/mapbox/${this.mapStyles[0].id}`; // default outdoor
  cursorStyle: string; // values could be '' or 'pointer'
  geocoderInitialized = false; // ugly, but too early in ngAfterViewInit - so when set? see applyFeatures

  coordinates: number[] = [18, 18]; // default center coordinates, [100.523186, 13.736717] = bangkok lon,lat style
  zoom = [MapComponent.DEFAULT_POI_ZOOM];
  accessToken = this.env.mapboxAccessToken;
  points: GeoJSON.FeatureCollection<GeoJSON.Point>;
  selectedPOI: MapboxGeoJSONFeature | null;

  poiLayerLayout = {
    'icon-image': '{icon}-15',
    'icon-allow-overlap': true,
    // zoom => size pairs for "interpolate" expressions must be arranged with input values in strictly ascending order.
    // Details https://stackoverflow.com/questions/61032600/scale-marker-size-relative-to-the-zoom-level-in-mapbox-gl-js
    'icon-size': ['interpolate', ['linear'], ['zoom'], 2, 1.0, 6, 1.5, 12, 3.0]
  };

  constructor(private env: EnvironmentService,
              private masterData: MasterDataService,
              private linkStore: LinkStoreService,
              private areaStore: AreaStoreService,
              private route: ActivatedRoute,
              private logger: NGXLogger) {
  }

  ngOnInit(): void {
    this.logger.debug(`${this.className}.ngOnInit: Ready to load map, token len=${this.env.mapboxAccessToken.length}`);

    // populate locationType2Maki which maps  api location type enum values to Maki identifiers
    this.masterData.getLocationTypes().forEach(locationType => {
      this.locationType2Maki.set(locationType.value, locationType.maki);
    });

    // check if component is called with coordinates e.g. http://localhost:4200/map/@14.067381,103.0984788
    if (this.route.snapshot.params.coordinates) {
      const match = this.route.snapshot.params.coordinates.match(REGEXP_COORDINATES); // match[1]=lat, match[2]=lon or match==null
      if (match != null) {
        this.logger.info(`${this.className} Zooming in to lat=${match[1]} lon=${match[2]}`);
        this.coordinates = [match[2] as number, match[1] as number];
        this.zoom = [MapComponent.DEEPLINK_POI_ZOOM]; // zoom in
      } else {
        this.logger.warn(`${this.className} ${this.route.snapshot.params.coordinates} does not match regexp ${REGEXP_COORDINATES}`);
      }
    }
    const queryParams = this.route.snapshot.queryParamMap;
    const feature = queryParams.has('from') ? queryParams.get('from') : null;
    switch (feature) {
      case 'videos':
        this.logger.debug('Feature: Video Mode, using exclusive display');
        this.initVideos(queryParams.has('id') ? queryParams.get('id') : null);
        break;
      case 'komoot-tours':
        this.logger.debug('Feature: Komoot Tour mode, show only tour links');
        this.initKomootTours(queryParams.has('id') ? queryParams.get('id') : null);
        break;
      case 'dishes':
        this.logger.debug('Feature: Dishes mode, delegate to standard mode POI');
        this.initCountries(queryParams.get('areaCode'));
        break;
      case 'places':
        this.logger.debug('Feature: Places mode, delegate to standard mode POI');
        this.initPlaces2Go();
        break;
      default:
          this.logger.debug('Feature: Default mode POI');
          this.initPlaces2Go(); // includes 'places' mode
    }

  }

  initCountries(areaCode?: string): void {
    this.logger.debug(`Country Display areaCode=${areaCode}`);
    if (areaCode) {
      this.masterData.countries.subscribe(areas => {
        for (const area of areas) {
          if ((area.coordinates?.length > 0) && area.code === areaCode) {
            this.logger.info(`Area ${area.name} matches and has coordinates, let's zoom in`);
            this.coordinates = area.coordinates;
            this.zoom = [MapComponent.ON_CLICK_POI_ZOOM];
            // Add item to lis
            const features: Array<Feature<GeoJSON.Point>> = []; // we'll push to this array while iterating through all POIs
            features.push({
              type: 'Feature',
              properties: {
                name: 'Country Location',
                areaCode,
                imageUrl: '',
                icon: 'attraction'
              },
              geometry: {
                type: 'Point',
                coordinates: area.coordinates
              }
            });
            this.points = {type: 'FeatureCollection', features};
            break;
          }
        }
      });
    }
  }

  // Experimental Youtube Video Location Layer ...
  initVideos(id?: string): void {

    // check if other components linked into map e.g. with ?from=somewhere
    const features: Array<Feature<GeoJSON.Point>> = []; // we'll push to this array while iterating through all POIs
    this.linkStore.getVideo$()
      .subscribe(videos => {
        videos.filter(video => video.coordinates?.length > 1)
          .forEach(video =>
            features.push({
              type: 'Feature',
              properties: {
                name: video.name + (video.id === id ? ' *' : ''), // cheap marker for the video we focus on, we can do better
                areaCode: null,
                // imageUrl: '/assets/icons/camera.svg',
                // use predictive youtube URLs https://stackoverflow.com/a/20542029/4292075, mq will be 320px, hq is 480, default is 120 thumb
                imageUrl: `https://img.youtube.com/vi/${video.youtubeId}/mqdefault.jpg`,
                routerLink: `/videos/${video.id}`,
                icon: 'cinema'
              },
              geometry: {type: 'Point', coordinates: video.coordinates}
            })
          );
        this.applyFeatures(features);
      });
  }

  // Experimental Komoot Tour Layer ...
  initKomootTours(id?: string): void {
    // check if other components linked into map e.g. with ?from=somewhere
    const features: Array<Feature<GeoJSON.Point>> = []; // we'll push to this array while iterating through all POIs
    this.linkStore.getKomootTours$()
      .subscribe(tours => {
        tours.filter(tour => tour.coordinates?.length > 1)
          .forEach(tour =>
            features.push({
              type: 'Feature',
              properties: {
                name: tour.name + (tour.id === id ? ' *' : ''), // cheap marker for the video we focus on, we can do better
                areaCode: null,
                imageUrl: tour.linkUrl + '/embed?image=1&profile=1',
                // routerLink: tour.linkUrl,
                icon: 'veterinary'
              },
              geometry: {type: 'Point', coordinates: tour.coordinates}
            })
          );
        this.applyFeatures(features);
      }); // end subscription callback
  }

  // Default: Load POIs for Places 2 Go
  initPlaces2Go(): void {
    // Load POIs from backend and put them on the map
    this.areaStore.getPOIs()
      .subscribe((poiList: POI[]) => {
        const features: Array<Feature<GeoJSON.Point>> = []; // we'll push to this array while iterating through all POIs

        poiList.forEach(poi => {
          if (!poi.coordinates) {
            this.logger.warn(`${this.className} ${poi.id} empty coordinates, skipping`);
            return;
          }

          features.push({
            type: 'Feature',
            properties: {
              name: poi.name,
              areaCode: poi.areaCode,
              imageUrl: this.getThumbnail(poi.imageUrl),
              routerLink: `/places/details/${poi.id}`,
              icon: this.getMakiIcon(poi.locationType)
            },
            geometry: {
              type: 'Point',
              coordinates: poi.coordinates
            }
          });
        }); // end poiList loop
        this.applyFeatures(features);
      }); // end subscription callback
  }

  // Set the GeoJSON.FeatureCollection which is bound to
  // <mgl-geojson-source /> element using [data]
  private applyFeatures(features: Array<Feature<GeoJSON.Point>> ) {
    this.points = {
      type: 'FeatureCollection',
      features  // Object-literal shorthand, means "features: features"
    };
    if (!this.geocoderInitialized) {
      this.logger.info('Adding Geocoder Control');
      this.mapbox.mapInstance.addControl(new MapboxGeocoder({
        accessToken: this.accessToken,
        // Minimum number of characters to enter before results are shown and limit, default is 2 and 5
        minLength: 3,
        limit: 4,
        placeholder: 'Look around',
        // If `true`, a Marker will be added to the map at the location of the user-selected result, requires mapboxgl
        marker: false
        // A [mapbox-gl](https://github.com/mapbox/mapbox-gl-js) instance to use when creating Markers
        // mapboxgl: this.mapbox
      }),'top-left' );
      this.geocoderInitialized =true;
    }
  }

  // getMakiIcon returns the identifier for a Make Icon e.g. attraction
  // Full List of available icons: https://labs.mapbox.com/maki-icons/
  getMakiIcon(locationType: string) {
    return this.locationType2Maki.has(locationType) && this.locationType2Maki.get(locationType).length > 0
      ? this.locationType2Maki.get(locationType) : 'attraction';
  }

  getThumbnail(imageUrl: string): string {
    if (imageUrl === null || imageUrl === undefined || (!imageUrl.startsWith(environment.apiUrlImagine))) {
      return '';
    }
    return imageUrl.replace('?large', '?small');
  }

  // onMapboxStyleChange is triggered when the user selects a different style, e.g. switches to street view
  onMapboxStyleChange(entry: { [key: string]: any }) {
    this.logger.info(`${this.className} Switch to mapbox://styles/mapbox/${entry.id}`);
    this.mapStyle = 'mapbox://styles/mapbox/' + entry.id;
  }

  // onPOIClick manages the details popup when the user clicks on a map icon
  onPOIClick(evt: MapLayerMouseEvent) {
    // https://stackoverflow.com/questions/35614957/how-can-i-read-current-zoom-level-of-mapbox
    // https://wykks.github.io/ngx-mapbox-gl/demo/edit/center-on-symbol
    this.selectedPOI = evt.features[0];
    // center map at POI
    this.coordinates = (evt.features[0].geometry as Point).coordinates;
    const actualZoom = this.mapbox.mapInstance.getZoom();
    if (actualZoom < MapComponent.ON_CLICK_POI_ZOOM) {
      this.logger.debug(`${this.className} Current Zoom level is ${actualZoom}, zooming in to ${MapComponent.ON_CLICK_POI_ZOOM}`);
      this.zoom = [MapComponent.ON_CLICK_POI_ZOOM]; // zoom in
    }
  }

}
