// Karma configuration file, see link for more information
// https://karma-runner.github.io/1.0/config/configuration-file.html

module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      // https://blog.angulartraining.com/how-to-running-angular-tests-on-continuous-integration-servers-ad492219c08c
      require('karma-jasmine-html-reporter'),
      // require('karma-coverage-istanbul-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      clearContext: false // leave Jasmine Spec Runner output visible in browser
    },
    // coverageIstanbulReporter: {
    //   dir: require('path').join(__dirname, '../coverage/angkor-ui'),
    //   reports: ['html', 'lcovonly', 'text-summary'],
    //   fixWebpackSourcePaths: true
    // },
    // new https://stackoverflow.com/questions/64810302/angular-11-unit-test-code-coverage-is-now-breaking
    coverageReporter: {
      dir: require('path').join(__dirname, '../coverage'),
      reporters: [
        { type: 'html', subdir: 'html' },
        { type: 'lcov', subdir: 'lcov' }
        // { type: 'html' },
        // { type: 'lcov' }
      ],
      fixWebpackSourcePaths: true
    },
    reporters: ['progress', 'kjhtml', ],
    port: 9876,
    colors: true,
    // config.ERROR,
    logLevel: config.INFO,
    autoWatch: true,
    browsers: ['Chrome'],
    singleRun: false,
    restartOnFileChange: true
  });
};
