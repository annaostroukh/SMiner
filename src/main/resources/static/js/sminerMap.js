var module = angular.module("sminer.map", []);
module.controller("sminerMap", [ "$scope", "$http", function($scope, $http) {
    angular.extend($scope, {
        japan: {
            lat: 37.26,
            lng: 138.86,
            zoom: 4
        },
        defaults: {
            scrollWheelZoom: false
        }
    });
}]);
