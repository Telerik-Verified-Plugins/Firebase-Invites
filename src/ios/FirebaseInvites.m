#import <GoogleSignIn/GoogleSignIn.h>
#import "FirebaseInvites.h"

@implementation FirebaseInvites {
    id <FIRInviteBuilder> _inviteDialog;
    NSString *_sendInvitationCallbackId;
}

- (void)sendInvitation:(CDVInvokedUrlCommand *)command {

    NSDictionary* options = command.arguments[0];
    NSString *title = options[@"title"];
    NSString *message = options[@"message"];
    NSString *androidClientID = options[@"androidClientID"];

    _sendInvitationCallbackId = command.callbackId;

    _inviteDialog = [FIRInvites inviteDialog];
    [_inviteDialog setInviteDelegate:self];

    // NOTE: You must have the App Store ID set in your developer console project in order for invitations to successfully be sent.

    // A message hint for the dialog. Note this manifests differently depending on the received invitation type.
    // For example, in an email invite this appears as the subject.
    [_inviteDialog setMessage:message];

    // Title for the dialog, this is what the user sees before sending the invites.
    [_inviteDialog setTitle:title];

    // TODO optional properties
    [_inviteDialog setCallToActionText:@"CTA text"];
    [_inviteDialog setDescription:@"My description"];
    [_inviteDialog setDeepLink:@"app_url"];
    [_inviteDialog setCallToActionText:@"Install!"];
    [_inviteDialog setCustomImage:@"https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"];
    
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
