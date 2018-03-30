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
            service.getReachabilityPlot = function(epsilon, minPts) {
                return $http({
                    method: 'GET',
                    url: CONSTANTS.getReachabilityPlot,
                    params: { epsilon: epsilon,
                                minPts: minPts}
                });
            };
            return service;
}]);
