// template, used in Dockerfile or as reference for local version
// included by index.html
// make sure this file is also considered as asset in angular.json
window.env = {
  VERSION: '${VERSION}',
  MAT: '${MAPBOX_ACCESS_TOKEN}',
  IMPRINT_URL: '${IMPRINT_URL}'
};
