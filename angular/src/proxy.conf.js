// https://angular.io/guide/build#proxying-to-a-backend-server
// should be consistent with path in nginx/nginx.conf
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
  },
  {
    context: [
      "/imagine"
    ],
    // Use http://localhost:8090 to forward to own imagine instance
    // Use https://dev.timafe.net/ to use remote instance
    target: "http://localhost:8090",
    // target: "https://dev.timafe.net/",
    secure: false // if true, you get ERR_TLS_CERT_ALTNAME_INVALID from localhost, even though cert is valid
  }
]

module.exports = PROXY_CONFIG;
