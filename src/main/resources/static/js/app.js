'use strict'

var sminerApp = angular.module("sminer", ["sminer.controller", "sminer.service"]);
sminerApp.constant("CONSTANTS", {
    modFileUpload: "/rest/modFileUpload",
    extractStops: "/rest/extractstops",
    getReachabilityPlot: "/rest/reachabilityPlot"
});
console.log("loaded");