# cordova-plugin-firebase-dynamiclinks
> Cordova plugin for [Firebase Invites](https://firebase.google.com/docs/invites/) and [Firebase Dynamic Links](https://firebase.google.com/docs/dynamic-links/)
 
## Installation

    cordova plugin add cordova-plugin-firebase-dynamiclinks --save

## Supported Platforms

- iOS
- Android

## Methods

### onDynamicLink(_callback_)
Registers callback that is triggered on each dynamic link click.
```js
window.cordova.plugins.firebase.dynamiclinks.onDynamicLink(function(data) {
    console.log("Dynamic link click with data: ", data);
});
```

### sendInvitation(_config_)
Display invitation dialog.
```js
window.cordova.plugins.firebase.dynamiclinks.sendInvitation({
    deepLink: deepLink,
    title: dialogTitle,
    message: dialogMessage,
    callToActionText: actionButtonText,
    iosClientID: iosClientID,
    androidClientID: androidClientID
});
```

