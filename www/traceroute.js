cordova.define("cordova-plugin-traceroute.traceroute", function(require, exports, module) {

var utils = require('cordova/utils'),
  exec = require('cordova/exec'),
  cordova = require('cordova');

function Traceroute (ipList ,ttl) {
  this.results = null;
}

Traceroute.prototype.ping = function (ipList, ttl, success, err) {
  var successCallback, errorCallback, self;
  self = this;
  successCallback = function (r) {
    self.results = r;
    if (success && typeof success === 'function') {
      success(r);
    }
  };
  errorCallback = function (e) {
    if (err && typeof err === 'function') {
      err(e);
    }
  };
  exec(successCallback, errorCallback, "Traceroute", "getPingInfo", [ipList, ttl]);
};

module.exports = Traceroute;

});
