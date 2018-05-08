'use strict'

var sminerApp = angular.module("sminer", ["sminer.controller", "sminer.service"]);
sminerApp.constant("CONSTANTS", {
    modFileUpload: "/rest/modFileUpload",
    extractStops: "/rest/extractstops",
    filterStopsByTime: "/rest/extractstopsByTime",
    getTemporalReachabilityPlot: "/rest/temporalReachabilityPlot",
    getSpatialReachabilityPlot: "/rest/spatialReachabilityPlot",
    getSpatialTemporalReachabilityPlot: "/rest/spatialTemporalReachabilityPlot",
    getSTDBSCANData: "/rest/stdbscan",
    setConfiguration: "/rest/datasetConfiguration"
});
