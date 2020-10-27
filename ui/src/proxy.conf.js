// https://angular.io/guide/build#proxying-to-a-backend-server
// should be consistent with pathes in nginx/nginx.conf
const PROXY_CONFIG = [
  {
    context: [
      "/api",
      "/auth",
      "/oauth2",
      "/login",
      "/actuator"
    ],
    target: "http://localhost:8080",
    secure: false
  }
]

module.exports = PROXY_CONFIG;
