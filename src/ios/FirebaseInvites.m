#import <GoogleSignIn/GoogleSignIn.h>
#import "FirebaseInvites.h"

@implementation FirebaseInvites {
    id <FIRInviteBuilder> _inviteDialog;
    NSString *_sendInvitationCallbackId;
}

- (void)getInvitation:(CDVInvokedUrlCommand *)command {
  if (self.cachedInvitation) {
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:self.cachedInvitation] callbackId:command.callbackId];
    self.cachedInvitation = nil;
  } else {
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Not launched by invitation"] callbackId:command.callbackId];
  }
}

// NOTE: You must have the App Store ID set in your developer console project in order for invitations to successfully be sent.
- (void)sendInvitation:(CDVInvokedUrlCommand *)command {
    NSDictionary* options = command.arguments[0];

    // Only title and message properties are mandatory (and checked in JS API)
    NSString *title = options[@"title"];
    NSString *message = options[@"message"];

    NSString *deepLink = options[@"deepLink"];
    NSString *callToActionText = options[@"callToActionText"];
    NSString *customImage = options[@"customImage"];
    NSString *description = options[@"description"];
    NSString *androidClientID = options[@"androidClientID"];

    _sendInvitationCallbackId = command.callbackId;

    _inviteDialog = [FIRInvites inviteDialog];
    [_inviteDialog setInviteDelegate:self];


    // A message hint for the dialog. Note this manifests differently depending on the received invitation type.
    // For example, in an email invite this appears as the subject.
    [_inviteDialog setMessage:message];

    // Title for the dialog, this is what the user sees before sending the invites.
    [_inviteDialog setTitle:title];

    [_inviteDialog setDescription:description];
    [_inviteDialog setDeepLink:deepLink];
    [_inviteDialog setCallToActionText:callToActionText];
    [_inviteDialog setCustomImage:customImage];
    
    // in case an Android app is available as well:
    if (androidClientID) {
        FIRInvitesTargetApplication *targetApplication = [FIRInvitesTargetApplication new];
        // The Android client ID from the Google API console project (?)
        targetApplication.androidClientID = androidClientID;
        [_inviteDialog setOtherPlatformsTargetApplication:targetApplication];
    }
    
    [_inviteDialog open];
}

#pragma mark FIRInviteDelegate
- (void)inviteFinishedWithInvitations:(NSArray *)invitationIds
                                error:(nullable NSError *)error
{
    __block CDVPluginResult *pluginResult;
    if (error) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
    } else {
        NSDictionary *result = @{
                                 @"count": @(invitationIds.count),
                                 @"invitationIds": invitationIds
                                 };
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:_sendInvitationCallbackId];
}

@end
