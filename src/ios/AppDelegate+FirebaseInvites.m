#import "AppDelegate+FirebaseInvites.h"
#import "FirebaseInvites.h"
#import <objc/runtime.h>

@implementation AppDelegate (FirebasePlugin)

+ (void)load {

    method_exchangeImplementations(
            class_getInstanceMethod(self, @selector(application:didFinishLaunchingWithOptions:)),
            class_getInstanceMethod(self, @selector(application:swizzledDidFinishLaunchingWithOptions:))
    );

    method_exchangeImplementations(
            class_getInstanceMethod(self, @selector(application:openURL:sourceApplication:annotation:)),
            class_getInstanceMethod(self, @selector(identity_application:openURL:sourceApplication:annotation:))
    );
}

- (BOOL)application:(UIApplication *)application swizzledDidFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    [self application:application swizzledDidFinishLaunchingWithOptions:launchOptions];
    if([FIRApp defaultApp] == nil){
        [FIRApp configure];
    }
    return YES;
}

- (BOOL)identity_application: (UIApplication *)application
                     openURL: (NSURL *)url
           sourceApplication: (NSString *)sourceApplication
                  annotation: (id)annotation {

    // Handle App Invite requests
    FIRReceivedInvite *invite = [FIRInvites handleURL:url sourceApplication:sourceApplication annotation:annotation];
    if (invite) {
        NSString *matchType = (invite.matchType == FIRReceivedInviteMatchTypeWeak) ? @"Weak" : @"Strong";

        FirebaseInvites *fbInvites = [self.viewController getCommandInstance:@"FirebaseInvites"];
        fbInvites.cachedInvitation = @{
                                       @"deepLink": invite.deepLink,
                                       @"invitationId": invite.inviteId,
                                       @"matchType": matchType
                                       };
        return YES;
    }

    // Note that if the GooglePlus.m version is not called, we can do this here (but seems ok):
//    return [[GIDSignIn sharedInstance] handleURL:url sourceApplication:sourceApplication annotation:annotation];

    // call super
    return [self identity_application:application openURL:url sourceApplication:sourceApplication annotation:annotation];
}

@end
