# About

The Firebase Invites plugin allows your users to leverage their Google account to send invites to contacts on their phone by E-mail or SMS.

# Use when

-   you want to spread the word about your awesome app,
-   but don't want to pay big $$ for advertisments,
-   but instead turn your app's users into your app's advocates.

# Documentation

## Firebase what?!

Check [this nice and short video](https://www.youtube.com/watch?v=LkaIJCZ_HyM) and check the [official docs](https://firebase.google.com/docs/invites/) afterwards to learn all about Firebase Invites. Make sure you read it thoroughly since we're not going to repeat most of the required configuration here.

## How this plugin works

Firebase Invites has two main features: sending invites from your app, and (optionally, upon launch) handling incoming invites. To be able to send invites the user needs to be logged on with a Google account. On Android this is a given but on iOS you'll need to connect your users through Google Sign-In. That's the reason this plugin adds the [Google Sign-In plugin](https://web.archive.org/web/20170911135317/http://plugins.telerik.com/cordova/plugin/google-sign-in) as a dependency automatically. Please refer to its doc to learn how to add the SHA-1 for Android and how to add Google Sign-In capabilities to your app.

## Firebase Invites setup

### IOS

Download the GoogleService-Info.plist file you find [here](https://developers.google.com/mobile/add?platform=ios&cntapi=signin) to your machine. You'll need to add this file to the root folder or www folder of your project before installing this plugin.

This plugin also requires you to set a plugin variable called REVERSED_CLIENT_ID which you can find inside the file you just downloaded.

Furthermore, you'll need to provide a variable called ASSOCIATED_DOMAIN which you can find in the Firebase Developer Console by clicking 'Dynamic Links' in the menu. You'll see a URL like 'https://s35v6.app.goo.gl' and need the 's35v6.app.goo.gl' part.

### ANDROID

To configure Android, [generate a configuration file here](https://developers.google.com/mobile/add?platform=android&cntapi=signin). This file also needs to be added to the root or www folder of your app.

## Sending invites from your app

For a description of the properties you can pass in to this function scroll down a bit [here (iOS)](https://firebase.google.com/docs/invites/ios) and [here (Android)](https://firebase.google.com/docs/invites/android). The property names are largely similar to the ones below.

```javascript
FirebaseInvites.sendInvitation(
  {
      title: "The title", // mandatory, see the screenshots for its purpose
      message: "The message", // mandatory, see the screenshots for its purpose
      deepLink: "myapp://deeplink",
      callToActionText: "My CTA",
      description: "My description",
      customImage: "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png",
      //emailSubject: "My Email subject",
      //emailHtmlContent: "Some <strong>HTML</strong> content",
      androidClientID: "123abc",
      // You can find your iOS app's client ID in the GoogleService-Info.plist file you downloaded from the Firebase console
      iosClientID: "abc123"
  },
  function (result) {
    console.log("Sent " + result.count + " invites");
    console.log("Invitation ID's: " + JSON.stringify(result.invitationIds));
  },
  function (msg) {
    alert("Error: " + msg);
  }
);
```

## Handling incoming app invites

If the user launced your app from an invite the information is cached by the plugin. You can simply ignore it or retrieve the launch details by calling this function and have your app respond to the specific Dynamic Link associated with the invite.

```javascript
FirebaseInvites.getInvitation(
  function (result) {
    console.log("invitation ID: " + result.invitationId);
    console.log("deeplink: " + result.deepLink);
    console.log("matchType: " + result.matchType); // iOS only, either "Weak" or "Strong" as described at https://firebase.google.com/docs/invites/ios
  },
  function (msg) {
    alert("Error: " + msg);
  }
);
```

# Sample App

[https://github.com/Telerik-Verified-Plugins/Firebase-Invites-DemoApp](https://github.com/Telerik-Verified-Plugins/Firebase-Invites-DemoApp)

# Repository

[https://github.com/Telerik-Verified-Plugins/Firebase-Invites](https://github.com/Telerik-Verified-Plugins/Firebase-Invites)


# Version History

-   1.0.1  Initial release