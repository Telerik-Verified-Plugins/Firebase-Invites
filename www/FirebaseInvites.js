
var exec = require('cordova/exec');

exports.sendInvitation = function(options, onSuccess, onError) {
    var opts = options || {};
    if (!opts.title || !opts.message) {
        var errorMsg = "Both 'title' and 'message' arguments are mandatory";
        if (typeof onError === "function") {
            onError(errorMsg);
        } else {
            console.error(errorMsg);
        }
        return;
    }
    exec(onSuccess, onError, "FirebaseInvites", "sendInvitation", [opts]);
};
