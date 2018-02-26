'use strict'

var sminerApp = angular.module("sminer", ["sminer.controller", "sminer.service"]);
sminerApp.constant("CONSTANTS", {
    modFileUpload: "/rest/modFileUpload",
    getUserByIdUrl: "/user/getUser/", //TODO: override with own url's
    getAllUsers: "/user/getAllUsers",
    saveUser: "/user/saveUser"
});
console.log("loaded");