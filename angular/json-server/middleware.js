const rootdir = 'json-server';
const fs = require('fs');
let places = fs.readFileSync(rootdir+'/places.json');
module.exports = function (req, res, next) {
  console.log('dirname', __dirname, req.method, req.originalUrl)
  if (req.method === 'POST' && req.originalUrl === '/places/search') {
    console.log('return custom json')
    return res.jsonp( JSON.parse(places))
  }
  next()
}
