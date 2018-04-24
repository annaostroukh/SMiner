'use strict'
angular.module("sminer.service",[]).factory("sminerService", ["$http", "CONSTANTS", function($http, CONSTANTS) {
    var service = {};
    /*service.getUserById = function(userId) {
        var url = CONSTANTS.getUserByIdUrl + userId;
        return $http.get(url);
    }*/
    return service;
}]).factory("documentService", ["$http", "CONSTANTS",
        function ($http, CONSTANTS) {
            var service = {};
            service.saveDoc = function(file) {
                var formData = new FormData();
                formData.append('file', file);
                var config = {
                    transformRequest: angular.identity,
                    transformResponse: angular.identity,
                    headers : {
                        'Content-Type': undefined
                    }
                };
                return $http.post(CONSTANTS.modFileUpload, formData, config);
            };
            service.saveConfiguration = function(modIdColumn, timestampColumn, longitudeColumn, latitudeColumn) {
                var datasetConfig = [modIdColumn, timestampColumn, longitudeColumn, latitudeColumn];
                var formData = new FormData();
                formData.append('configuration', datasetConfig);
                var config = {
                    transformRequest: angular.identity,
                    transformResponse: angular.identity,
                    headers : {
                        'Content-Type': undefined
                    }
                };
                return $http.post(CONSTANTS.setConfiguration, formData, config);

            };
            return service;
}]).factory("dataAnalysisService", ["$http", "CONSTANTS",
        function ($http, CONSTANTS) {
            var service = {};
            service.extractStops = function(minStopDuration, maxStopDuration) {
                return $http({
                    method: 'GET',
                    url: CONSTANTS.extractStops,
                    params: { minStopDuration: minStopDuration,
                                maxStopDuration: maxStopDuration}
                });
            };
            service.getTemporalReachabilityPlot = function(epsilonTemporal, minPtsTemporal) {
                return $http({
                    method: 'GET',
                    url: CONSTANTS.getTemporalReachabilityPlot,
                    params: { epsilonTemporal: epsilonTemporal,
                                minPtsTemporal: minPtsTemporal}
                });
            };
            service.getSpatialReachabilityPlot = function(epsilonSpatial, minPtsSpatial) {
                return $http({
                    method: 'GET',
                    url: CONSTANTS.getSpatialReachabilityPlot,
                    params: { epsilonSpatial: epsilonSpatial,
                        minPtsSpatial: minPtsSpatial}
                });
            };
            service.getSpatialTemporalReachabilityPlot = function(epsilonTemporal, epsilonSpatial, minPtsTemporal) {
                return $http({
                    method: 'GET',
                    url: CONSTANTS.getSpatialTemporalReachabilityPlot,
                    params: { epsilonTemporal: epsilonTemporal,
                        epsilonSpatial: epsilonSpatial,
                        minPtsTemporal: minPtsTemporal}
                });
            };
            service.getSTDBSCANData = function(epsilonTempSTDBSCAN, epsilonSpatialSTDBSCAN, minPtsSTDBSCAN) {
                return $http({
                    method: 'GET',
                    url: CONSTANTS.getSTDBSCANData,
                    params: { epsilonTempSTDBSCAN: epsilonTempSTDBSCAN,
                        epsilonSpatialSTDBSCAN: epsilonSpatialSTDBSCAN,
                        minPtsSTDBSCAN: minPtsSTDBSCAN}
                });
            };
            return service;
}]);
