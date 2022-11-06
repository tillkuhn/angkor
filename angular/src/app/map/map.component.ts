// Mapbox and Geo Imports we need to import the "official" MapComponent as an alias
// since we foolishly called also our own class "MapComponent" :-)
import MapboxGeocoder from '@mapbox/mapbox-gl-geocoder';
import {ActivatedRoute} from '@angular/router';
import {Component, OnInit, ViewChild} from '@angular/core';
import {EnvironmentService} from '@shared/services/environment.service';
// Since update to angular 14:./src/app/map/map.component.ts:4:0-41 - Error: Module not found: Error: Can't resolve 'geojson' in '/Users/tillkuhn/git/angkor/angular/src/app/map'
// new from location is geojson -> @mapbox/geojson-types
import {Feature, GeoJSON, Point} from '@mapbox/geojson-types';
import {MapComponent as MapboxGLMapComponent} from 'ngx-mapbox-gl';
import {MapboxGeoJSONFeature, MapLayerMouseEvent, Marker} from 'mapbox-gl';
import {MasterDataService} from '@shared/services/master-data.service';
import {NGXLogger} from 'ngx-logger';
import {POI} from '@domain/poi';
import {REGEXP_COORDINATES} from '@shared/domain/smart-coordinates';
import {environment} from '../../environments/environment';
import {EntityMetadata, EntityType} from '@shared/domain/entities';
import {GeoService} from '@app/map/geo.service';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit /* AfterViewInit */ {

  zoom = [GeoService.DEFAULT_POI_ZOOM];

  // Get access to the MapboxGLMapComponent
  // https://angular-2-training-book.rangle.io/advanced-components/access_child_components
  @ViewChild(MapboxGLMapComponent)
  mapbox: MapboxGLMapComponent;

  // mapStyles is an array of different map styles like outdoor, satellite to puck from
  // Check https://docs.mapbox.com/mapbox-gl-js/example/setstyle/ for code how to set via API
  // For alternative styles, streets-v11, check https://docs.mapbox.com/api/maps/#styles
  cursorStyle: string; // values could be '' or 'pointer'
  mapFullyInitialized = false; // ugly, but too early in ngAfterViewInit - so we set it applyFeatures *once*
  interactiveMarker = new Marker();
  coordinates: number[] = [18, 18]; // default center coordinates, [100.523186, 13.736717] = bangkok lon,lat style
  accessToken = this.env.mapboxAccessToken;

  // points are mapped to the mgl-geojson-source component's data property
  // and written by applyFeatures()
  points: GeoJSON.FeatureCollection<GeoJSON.Point>;

  poiLayerLayout = {
    // 'icon-image': '{icon}-15', // this works
    'icon-image': '{icon}',
    'icon-allow-overlap': true,
    // zoom => size pairs for "interpolate" expressions must be arranged with input values in strictly ascending order.
    // Details https://stackoverflow.com/questions/61032600/scale-marker-size-relative-to-the-zoom-level-in-mapbox-gl-js
    'icon-size': ['interpolate', ['linear'], ['zoom'], 2, 1.0, 6, 1.5, 12, 3.0]
  };
  // selectedPOI is updated by onPOIClick
  selectedPOI: MapboxGeoJSONFeature | null;

  private readonly className = 'MapComponent';
  private locationType2Maki: Map<string, string> = new Map();

  constructor(private env: EnvironmentService,
              private masterData: MasterDataService,
              public geoService: GeoService,
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
        this.zoom = [GeoService.DEEPLINK_POI_ZOOM]; // zoom in
      } else {
        this.logger.warn(`${this.className} ${this.route.snapshot.params.coordinates} does not match regexp ${REGEXP_COORDINATES}`);
      }
    }
    const queryParams = this.route.snapshot.queryParamMap;
    const feature = queryParams.has('from') ? queryParams.get('from') : null;
    switch (feature) {
      case 'videos':
        this.initPOIs(EntityType.Video); break;
      case 'posts':
        // this.initVideos(queryParams.has('id') ? queryParams.get('id') : null);
        this.initPosts(); break;
      case 'tours':
        this.initTours(); break;
      case 'photos':
        this.initPhotos(); break;
      case 'places':
        this.initPlaces(); break;
      case 'dishes':
        this.initCountries(queryParams.get('areaCode')); break;
      default:
        this.logger.debug('Feature: Default mode Place POI');
        this.initPlaces(); // includes 'places' mode
    }

  }

  /** Load countries for experimental cluster display */
  initCountries(areaCode?: string): void {
    this.logger.debug(`Country Display areaCode=${areaCode}`);
    if (areaCode) {
      this.masterData.countries.subscribe(areas => {
        for (const area of areas) {
          if ((area.coordinates?.length > 0) && area.code === areaCode) {
            this.logger.info(`Area ${area.name} matches and has coordinates, let's zoom in`);
            this.coordinates = area.coordinates;
            this.zoom = [GeoService.ON_CLICK_POI_ZOOM];
            // Add items to list of GeoJSON Points
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

  initPlaces() { this.initPOIs(EntityType.Place); }
  initTours() { this.initPOIs(EntityType.Tour); }
  initPosts() { this.initPOIs(EntityType.Post); }
  initVideos() { this.initPOIs(EntityType.Video); }
  initPhotos() { this.initPOIs(EntityType.Photo); }

  /** generic function to load POIs with a specific entity type filter */
  initPOIs(entityType: EntityType): void {
    this.logger.debug(`${this.className}initPOIs: Loading ${entityType} POIs`);
    // Load POIs from backend and put them on the map
    this.geoService.getPOIs(entityType)
      .subscribe((poiList: POI[]) => {
        const features: Array<Feature<GeoJSON.Point>> = []; // we'll push to this array while iterating through all POIs
        const eMeta =  EntityMetadata[entityType];
        poiList.forEach(poi => {
          if (!poi.coordinates) {
            this.logger.warn(`${this.className} ${poi.id} empty coordinates, skipping`);
            return;
          }
          // todo align places with "ordinary" locatables
          const detailsPath = (entityType === EntityType.Place) ? 'places/details' : eMeta.path;
          features.push({
            type: 'Feature',
            properties: {
              name: poi.name,
              areaCode: poi.areaCode,
              imageUrl: this.getThumbnail(poi.imageUrl),
              routerLink: `/${detailsPath}/${poi.id}`,
              // icon: this.getMakiIcon(poi.locationType)
              icon: eMeta.name,
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


  // /** returns the identifier for a Make Icon e.g. attraction, Full list: https://labs.mapbox.com/maki-icons/ */
  // getMakiIcon(locationType: string) {
  //   return this.locationType2Maki.has(locationType) && this.locationType2Maki.get(locationType).length > 0
  //     ? this.locationType2Maki.get(locationType) : 'attraction';
  // }

  /** returns the image small image url, or empty string if the url is something else (or empty) */
  // todo align with location search which needs similar logic
  getThumbnail(imageUrl: string): string {

    if (imageUrl === null || imageUrl === undefined) {
      return '';
    } else if (imageUrl.startsWith(environment.apiUrlImagine)) {
      return imageUrl.replace('?large', '?small');
    } else {
      // todo more preview support see function comment
      return imageUrl;
    }
  }

  /** onMapboxStyleChange is triggered when the user selects a different style, e.g. switches to street view */
  onMapboxStyleChange(entry: { [key: string]: any }) {
    this.logger.info(`${this.className} Switch to mapbox://styles/mapbox/${entry.id}`);
    this.geoService.mapStyle = 'mapbox://styles/mapbox/' + entry.id;
  }

  /** onPOIClick manages the details popup when the user clicks on a map icon */
  onPOIClick(evt: MapLayerMouseEvent) {
    // https://stackoverflow.com/questions/35614957/how-can-i-read-current-zoom-level-of-mapbox
    // https://wykks.github.io/ngx-mapbox-gl/demo/edit/center-on-symbol
    this.selectedPOI = evt.features[0];
    // center map at POI
    this.coordinates = (evt.features[0].geometry as Point).coordinates;
    const actualZoom = this.mapbox.mapInstance.getZoom();
    if (actualZoom < GeoService.ON_CLICK_POI_ZOOM) {
      this.logger.debug(`${this.className} Current Zoom level is ${actualZoom}, zooming in to ${GeoService.ON_CLICK_POI_ZOOM}`);
      this.zoom = [GeoService.ON_CLICK_POI_ZOOM]; // zoom in
    }
  }

  /** Init / update geojson-source with Points,
   * updates array of points which acts as <mgl-geojson-source /> element using [data]
   */
  private applyFeatures(features: Array<Feature<GeoJSON.Point>>) {
    // Set the GeoJSON.FeatureCollection which is bound to
    this.points = {
      type: 'FeatureCollection',
      features  // Object-literal shorthand, means "features: features"
    };
    // doOnce ...
    if (!this.mapFullyInitialized) {
      // Feature Request: SVG symbols (via addImage
      // https://github.com/mapbox/mapbox-gl-js/issues/5529#issuecomment-758266861
      this.logger.info(`${this.className}.applyFeatures: Adding custom icons`);
      const locatables = [EntityType.Video, EntityType.Place, EntityType.Photo, EntityType.Tour, EntityType.Post];
      locatables.forEach( locatable => {
        const customIcon = new Image(18, 18);
        const eMeta = EntityMetadata[locatable];
        customIcon.onload = () => this.mapbox.mapInstance.addImage( eMeta.name, customIcon);
        customIcon.src = eMeta.iconUrl;
      });

      // init geocoder search and addMarker onContextmenu behaviour. This seems the wrong place,
      // but to put it inside afterViewInit is too early as mapbox.mapInstance is still null
      this.logger.info(`${this.className}.applyFeatures: Adding Geocoder Control`);
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
      }), 'top-left');

      // add marker on double click, see https://github.com/mapbox/mapbox-gl-js/issues/9209
      this.mapbox.mapInstance.on('contextmenu', ev => {
          this.logger.info(`onClick map move Marker to lngLat ${ev.lngLat}`);
          this.interactiveMarker.setLngLat(ev.lngLat).addTo(this.mapbox.mapInstance);
        }
      );
      // set initialized, so we don't do this again on each applyFeatures call
      this.mapFullyInitialized = true;
    }
  }

}


