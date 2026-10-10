module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('@angular-devkit/build-angular/plugins/karma'),
    ],
    client: { jasmine: { random: false }, clearContext: false },
    jasmineHtmlReporter: { suppressAll: true },
    reporters: ['progress'],
    browsers: ['ChromeHeadlessNoSandbox'],
    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-dev-shm-usage', '--disable-gpu', '--headless=new'],
      },
    },
    singleRun: true,
    autoWatch: false,
    captureTimeout: 60000,
    browserDisconnectTolerance: 2,
    browserDisconnectTimeout: 10000,
    browserNoActivityTimeout: 60000,
    concurrency: 1,
  });
};
