const rootdir = 'json-server';
const fs = require('fs');
let places = fs.readFileSync(rootdir+'/places.json');
module.exports = function (req, res, next) {
  console.log(__dirname)
  if (req.method === 'POST' && req.originalUrl === '/api/v1/places/search') {
    return res.jsonp( JSON.parse(places))
  }
  next()
}
