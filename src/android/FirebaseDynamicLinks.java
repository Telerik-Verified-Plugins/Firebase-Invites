package by.chemerisuk.cordova.firebase;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.appinvite.AppInviteInvitation.IntentBuilder.PlatformMode.PROJECT_PLATFORM_IOS;

public class FirebaseInvites extends CordovaPlugin implements GoogleApiClient.OnConnectionFailedListener {
  private static final String TAG = "FirebaseInvites";

  private static final String ACTION_SEND_INVITATION = "sendInvitation";
  private static final String ACTION_GET_INVITATION = "onDynamicLink";
  private static final String ACTION_CONVERT_INVITATION = "convertInvitation";

  private static final int REQUEST_INVITE = 48;

  private GoogleApiClient _googleApiClient;
  private CallbackContext _sendInvitationCallbackContext;
  private CallbackContext _getInvitationCallbackContext;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (ACTION_SEND_INVITATION.equals(action)) {
      sendInvitation(callbackContext, args.getJSONObject(0));
      return true;

    } else if (ACTION_GET_INVITATION.equals(action)) {
      getInvitation(callbackContext);
      return true;
    } else if (ACTION_CONVERT_INVITATION.equals(action)) {
      convertInvitation(callbackContext, args.getString(0));
      return true;
    }

    return false;
  }

  @Override
  public void onNewIntent(Intent intent) {
      super.onNewIntent(intent);

      if (AppInviteReferral.hasReferral(intent)) {
        respondWithDeepLink(intent);
      }
  }

  private void getInvitation(final CallbackContext callbackContext) {
    this._getInvitationCallbackContext = callbackContext;

    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        Intent activityIntent = cordova.getActivity().getIntent();

        if (AppInviteReferral.hasReferral(activityIntent)) {
          respondWithDeepLink(activityIntent);
        } else {
          //noinspection ConstantConditions (added for clarity)
          AppInvite.AppInviteApi.getInvitation(getGoogleApiClient(), cordova.getActivity(), false)
            .setResultCallback(new ResultCallback<AppInviteInvitationResult>() {
              @Override
              public void onResult(@NonNull AppInviteInvitationResult result) {
                if (result.getStatus().isSuccess()) {
                  respondWithDeepLink(result.getInvitationIntent());
                } else {
                  _getInvitationCallbackContext.error("Not launched by invitation");
                }
              }
            });
        }
      }
    });
  }

  private void respondWithDeepLink(Intent intent) {
    try {
      JSONObject response = new JSONObject();
      String invitationId = AppInviteReferral.getInvitationId(intent);

      if (invitationId != null) {
        response.put("invitationId", invitationId);
      }

      response.put("deepLink", AppInviteReferral.getDeepLink(intent));

      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, response);
      pluginResult.setKeepCallback(true);
      _getInvitationCallbackContext.sendPluginResult(pluginResult);
    } catch (JSONException e) {
      Log.e(TAG, "Fail to handle dynamic link data", e);
    }
  }

  private void sendInvitation(final CallbackContext callbackContext, final JSONObject options) {
    this._sendInvitationCallbackContext = callbackContext; // only used for onActivityResult
    final FirebaseInvites that = this;

    cordova.getThreadPool().execute(
        new Runnable() {
          @Override
          public void run() {
            try {
              // Only title and message properties are mandatory (and checked in JS API)
              // For all properties, see https://firebase.google.com/docs/invites/android
              final String title = options.getString("title");
              final String message = options.getString("message");
              AppInviteInvitation.IntentBuilder builder = new AppInviteInvitation.IntentBuilder(title).setMessage(message);

              if (options.has("deepLink")) {
                builder.setDeepLink(Uri.parse(options.getString("deepLink")));
              }

              if (options.has("callToActionText")) {
                builder.setCallToActionText(options.getString("callToActionText"));
              }

              if (options.has("customImage")) {
                builder.setCustomImage(Uri.parse(options.getString("customImage")));
              }

              if (options.has("emailSubject")) {
                builder.setEmailSubject(options.getString("emailSubject"));
              }

              if (options.has("emailHtmlContent")) {
                builder.setEmailHtmlContent(options.getString("emailHtmlContent"));
              }

              if (options.has("iosClientID")) {
                builder.setOtherPlatformsTargetApplication(PROJECT_PLATFORM_IOS, options.getString("iosClientID"));
              }

              if (options.has("androidMinimumVersion")) {
                builder.setAndroidMinimumVersionCode(options.getInt("androidMinimumVersion"));
              }

              final Intent intent = builder.build();
              cordova.startActivityForResult(that, intent, REQUEST_INVITE);

            } catch (JSONException e) {
              _sendInvitationCallbackContext.error(e.getMessage());
            }
          }
        }
    );
  }

  private void convertInvitation(final CallbackContext callbackContext, final String invitationId) {
    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        AppInvite.AppInviteApi.convertInvitation(getGoogleApiClient(), invitationId)
          .setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status result) {
              if (result.isSuccess()) {
                callbackContext.success();
              } else {
                callbackContext.error("convertInvitation failed with error code " + result.getStatusCode());
              }
            }
          });
      }
    });
  }

  private GoogleApiClient getGoogleApiClient() {
    if (this._googleApiClient == null) {
      this._googleApiClient = new GoogleApiClient.Builder(webView.getContext())
          .addOnConnectionFailedListener(this)
          .addApi(AppInvite.API)
          .build();
    }

    return this._googleApiClient;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);

    if (_sendInvitationCallbackContext == null) {
      return;
    }

    if (requestCode == REQUEST_INVITE) {
      if (resultCode == RESULT_OK) {
        final String[] ids = AppInviteInvitation.getInvitationIds(resultCode, intent);
        try {
          JSONObject response = new JSONObject()
              .put("count", ids.length)
              .put("invitationIds", new JSONArray(ids));
          _sendInvitationCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, response));
        } catch (JSONException e) {
          _sendInvitationCallbackContext.error(e.getMessage());
        }
      } else {
        if (resultCode == 3) {
          _sendInvitationCallbackContext.error("Resultcode 3, see http://stackoverflow.com/questions/37883664/result-code-3-when-implementing-appinvites");
        } else {
          _sendInvitationCallbackContext.error("Resultcode: " + resultCode);
        }
      }
    }
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult result) {
    this._getInvitationCallbackContext.error(
        "Connection to Google API failed with errorcode: " + result.getErrorCode());
  }
}
