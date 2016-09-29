package com.telerik.plugins.firebaseinvites;

import android.content.Intent;

import com.google.android.gms.appinvite.AppInviteInvitation;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;

public class FirebaseInvites extends CordovaPlugin {

  private static final String ACTION_SEND_INVITATION = "sendInvitation";
  private static final int REQUEST_INVITE = 48;

  private CallbackContext _callbackContext;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (ACTION_SEND_INVITATION.equals(action)) {
      sendInvitation(callbackContext, args.getJSONObject(0));
      return true;
    }
    return false;
  }

  private void sendInvitation(CallbackContext callbackContext, JSONObject options) throws JSONException {
    final String title = options.getString("title");
    final String message = options.getString("message");

    this._callbackContext = callbackContext; // only used for onActivityResult

    // For all properties, see https://firebase.google.com/docs/invites/android
    Intent intent = new AppInviteInvitation.IntentBuilder(title)
        .setMessage(message)
//        .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
//        .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
//        .setCallToActionText(getString(R.string.invitation_cta))
        .build();
    cordova.startActivityForResult(this, intent, REQUEST_INVITE);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
//    Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

    if (_callbackContext == null) {
      return;
    }

    // TODO see https://github.com/EddyVerbruggen/SocialSharing-PhoneGap-Plugin/blob/master/src/android/nl/xservices/plugins/SocialSharing.java#L680
    if (requestCode == REQUEST_INVITE) {
      if (resultCode == RESULT_OK) {
        // Get the invitation IDs of all sent messages
        String[] ids = AppInviteInvitation.getInvitationIds(resultCode, intent);
        for (String id : ids) {
//          Log.d(TAG, "onActivityResult: sent invitation " + id);
        }
      } else {
        // TODO add SHA-1, etc -- see http://stackoverflow.com/questions/37883664/result-code-3-when-implementing-appinvites
        // Sending failed or it was canceled, show failure message to the user
        // ...
      }
    }
  }
}
