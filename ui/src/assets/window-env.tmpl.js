// template, used in Dockerfile or as reference for local version
// included by index.html
// make sure this file is also listed as asset in
window.env = {
  VERSION: '${VERSION}',
  MAT: '${MAPBOX_ACCESS_TOKEN}'
};
