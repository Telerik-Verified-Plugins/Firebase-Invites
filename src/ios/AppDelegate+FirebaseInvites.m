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

    [FIRApp configure];

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
        NSString *message = [NSString stringWithFormat:@"Deep link from %@ \nInvite ID: %@\nApp URL: %@\nMatch Type:%@",
                                           sourceApplication, invite.inviteId, invite.deepLink, matchType];

        [[[UIAlertView alloc] initWithTitle:@"Deep-link Data"
                                    message:message
                                   delegate:nil
                          cancelButtonTitle:@"OK"
                          otherButtonTitles:nil] show];

        return YES;
    }

    // if the GooglePlus.m version is not called, we can do this here:
//    return [[GIDSignIn sharedInstance] handleURL:url sourceApplication:sourceApplication annotation:annotation];

    // call super
    return [self identity_application:application openURL:url sourceApplication:sourceApplication annotation:annotation];
}

@end
