// template, used in Dockerfile or as reference for local version
// included by index.html
// make sure this file is also considered as asset in angular.json
window.env = {
  APP_VERSION: '${APP_VERSION}',
  RELEASE_NAME: '${RELEASE_NAME}',
  UI_STARTED: '${UI_STARTED}',
  MAT: '${MAPBOX_ACCESS_TOKEN}',
  IMPRINT_URL: '${IMPRINT_URL}'
};
