#import <Cordova/CDV.h>
#import "AppDelegate.h"
#import "Firebase.h"

@interface FirebaseInvites : CDVPlugin<FIRInviteDelegate>

@property NSDictionary* cachedInvitation;

- (void)getInvitation:(CDVInvokedUrlCommand *)command;
- (void)sendInvitation:(CDVInvokedUrlCommand*)command;
@end
