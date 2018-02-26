'use strict'
var module = angular.module("sminer.controller", ["ui-leaflet", "angular-loading-bar"]);
module.controller("sminerController", ["$scope", "$http", "$rootScope", "CONSTANTS", "sminerService", "documentService",
    function($scope, $http, $rootScope, CONSTANTS, sminerService, documentService) {
        $scope.fileChosen = false;
        $scope.uploadingInProgress = false;
        $scope.fileLoaded = false;
        $scope.dataSetAnalysisDone = false;

        // Map
        angular.extend($scope, {
            bounds: {
                address: 'Bath, UK'
            }
        });

        $scope.getMbSize = function(bytes){
            return Math.round(bytes / 1000000);
        };

        // MOD file upload
        $scope.uploadFile = function(){
            $scope.uploadingInProgress = true;
            documentService.saveDoc($scope.uploadedFile).then(
                function (response) {
                    $scope.uploadingInProgress = false;
                    $scope.fileStats = angular.fromJson(response.data);
                    $scope.fileLoaded = true;
                },
                function (errResponse) {
                    alert(errResponse.data.errorMessage);
                    $scope.uploadingInProgress = false;
                    $scope.uploadResult = errResponse.data;
                }
            );
        };
}]);

module.directive("fileModel", ["$parse", function ($parse) {
    return {
        restrict: "A",
        link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind("change", function(){
                scope.$apply(function(){
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);

module.config(["cfpLoadingBarProvider", function(cfpLoadingBarProvider) {
    cfpLoadingBarProvider.parentSelector = '#loading-bar-container';
    cfpLoadingBarProvider.spinnerTemplate = '<div><span class="fa fa-spinner">Uploading the dataset...</div>';
}]);