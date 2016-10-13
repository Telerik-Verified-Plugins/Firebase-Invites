#import <Cordova/CDV.h>
#import "AppDelegate.h"
#import "Firebase.h"

@interface FirebaseDynamicLinks : CDVPlugin<FIRInviteDelegate>

+ (FirebaseDynamicLinks *) instance;
- (void)onDynamicLink:(CDVInvokedUrlCommand *)command;
- (void)sendInvitation:(CDVInvokedUrlCommand*)command;
- (void)convertInvitation:(CDVInvokedUrlCommand*)command;
- (void)sendDynamicLinkData:(NSDictionary*)data;

@property (nonatomic, copy) NSString *dynamicLinkCallbackId;
@property NSDictionary* cachedInvitation;

@end
