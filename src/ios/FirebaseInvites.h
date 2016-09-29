#import <Cordova/CDV.h>
#import "AppDelegate.h"
#import "Firebase.h"

@interface FirebaseInvites : CDVPlugin<FIRInviteDelegate>
- (void)sendInvitation:(CDVInvokedUrlCommand*)command;
@end
