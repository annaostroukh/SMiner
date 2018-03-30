'use strict'
var module = angular.module("sminer.controller", ["ui-leaflet", "zingchart-angularjs" /*"angular-loading-bar"*/]);
module.controller("sminerController", ["$scope", "$http", "$rootScope", "CONSTANTS", "sminerService", "documentService",
    "dataAnalysisService",
    function($scope, $http, $rootScope, CONSTANTS, sminerService, documentService, dataAnalysisService) {
        $scope.fileChosen = false;
        $scope.uploadingInProgress = false;
        $scope.extractingStopsInProgress = false;
        $scope.reachabilityPlotInProgress = false;
        $scope.fileLoaded = false;
        $scope.stopsExtracted = false;
        $scope.dataSetAnalysisDone = false;
        $scope.reachabilityPlotReady = false;

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

        $scope.extractStops = function() {
            $scope.extractingStopsInProgress = true;
            dataAnalysisService.extractStops($scope.minStopDuration, $scope.maxStopDuration).then(
                function(response) {
                    $scope.extractingStopsInProgress = false;
                    $scope.stopsAmount = response.data.value;
                    $scope.stopsExtracted = true;
                },
                function(errResponse) {
                    alert(errResponse.data.errorMessage);
                    $scope.extractingStopsInProgress = false;
                    $scope.stopsExtracted = false;
                }
            )
        }

        $scope.getReachabilityPlot = function() {
            $scope.reachabilityPlotInProgress = true;
            dataAnalysisService.getReachabilityPlot($scope.epsilon, $scope.minPts).then(
                function(response) {
                    var labels = [];
                    var data = [];
                    $scope.reachabilityPlotInProgress = false;
                    $scope.reachabilityPlotReady = true;
                    $scope.plotData = response.data.data;
                    angular.forEach($scope.plotData, function(value, key) {
                        labels.push(key);
                        data.push(value == 0 ? null : value);
                    });
                    $scope.myJson = {
                        type: "bar",
                        title: {
                            text: "Temporal reachability plot",
                            "font-family": "Segoe UI",
                            "font-size": 18
                        },
                        "crosshair-y": {
                            "line-color": "#FF7F27",
                            "line-width": 2,
                            "line-style": "dashed"
                        },
                        stacked: true,
                        scrollX:{
                        },
                        preview:{
                            height: "20%",
                            width: "100%",
                            x: "5%",
                            y: "7%"
                        },
                        plot:{
                            aspect: "histogram",
                            alphaArea: 0.6,
                            tooltip: {
                                visible: false
                            }
                            //styles: getPlotStyles(data)
                        },
                        plotarea:{
                            "margin-top": "30%",
                            "margin-bottom": "10%"
                        },
                        scaleX: {
                            zooming: true,
                            values: labels,
                            "zoom-to":[0,50],
                            label:{
                                text: "Points order",
                                "font-family": "Segoe UI",
                                "font-size": 14
                            },
                        },
                        scaleY: {
                            label:{
                                text: "Stop duration (min)",
                                "font-family": "Segoe UI",
                                "font-size": 14
                            }
                        },
                        series: [{
                            values: data
                        }]
                    };
                },
                function(errResponse) {
                    alert(errResponse.data.errorMessage);
                    $scope.reachabilityPlotInProgress = false;
                    $scope.reachabilityPlotReady = false;
                }
            )
        }

        /*var getPlotStyles = function(data) {
            var styles =[];
            data.forEach(function (value){
                if (value == -1) {
                    styles.push("#ED1C24");
                } else {
                    styles.push("#1E7FA8");
                }
            });
            return styles;
        };*/
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

/*module.config(['ChartJsProvider', function (ChartJsProvider) {
    // Configure all charts
    ChartJsProvider.setOptions({
        chartColors: ['#FF5252', '#FF8A80'],
        responsive: false
    });
    // Configure all line charts
    ChartJsProvider.setOptions('line', {
        showLines: false
    });
}]).controller("LineCtrl", ['$scope', '$timeout', function ($scope, $timeout) {

    $scope.labels = ["January", "February", "March", "April", "May", "June", "July"];
    $scope.series = ['Series A', 'Series B'];
    $scope.data = [
        [65, 59, 80, 81, 56, 55, 40],
        [28, 48, 40, 19, 86, 27, 90]
    ];
    $scope.onClick = function (points, evt) {
        console.log(points, evt);
    };

    // Simulate async data update
    $timeout(function () {
        $scope.data = [
            [28, 48, 40, 19, 86, 27, 90],
            [65, 59, 80, 81, 56, 55, 40]
        ];
    }, 3000);
}]);

/*module.config(["cfpLoadingBarProvider", function(cfpLoadingBarProvider) {
    cfpLoadingBarProvider.parentSelector = '#loading-bar-container';
    cfpLoadingBarProvider.spinnerTemplate = '<div><span class="fa fa-spinner">Uploading the dataset...</div>';
}]);*/