/*global cordova, module*/

module.exports = {
    getMusicInfo: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CurrentlyPlaying", "getMusicInfo", [name]);
    }
};
