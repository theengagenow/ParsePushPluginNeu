package com.theengagenow.ParsePushPluginNeu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONObject;

/**
 * Created by Engagenow Data Sciences Pvt. Ltd. 19/04/2021.
 */

public class ParsePushPluginNeuReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "com.theengagenow.ParsePushPluginNeuReceiver";
    /**
     * The name of the Intent fired when a push has been received.
     */
    public static final String ACTION_PUSH_RECEIVE = "com.parse.push.intent.RECEIVE";
    /**
     * The name of the Intent fired when a notification has been opened.
     */
    public static final String ACTION_PUSH_OPEN = "com.parse.push.intent.OPEN";

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction != null) {
            switch (intentAction) {
                case ACTION_PUSH_RECEIVE:
                    onPushReceive(context, intent);
                    break;
                case ACTION_PUSH_OPEN:
                    onPushOpen(context, intent);
                    break;
            }
        }
    }


    /**
     * Called when the push notification is received. By default, a broadcast intent will be sent if
     * an "action" is present in the data and a notification will be show if "alert" and "title" are
     * present in the data.
     *
     * @param context The {@code Context} in which the receiver is running.
     * @param intent  An {@code Intent} containing the channel and data of the current push notification.
     */
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context,intent);
        if (ParsePushPluginNeu.isInForeground()) {
            //
            // relay the push notification data to the javascript
            ParsePushPluginNeu.jsCallback(getPushData(intent));
        }
    }

    /**
     * Called when the push notification is opened by the user. Sends analytics info back to Parse
     * that the application was opened from this push notification. By default, this will navigate
     * to the {@link Activity} returned by {@link #getActivity(Context, Intent)}. If the push contains
     * a 'uri' parameter, an Intent is fired to view that URI with the Activity returned by
     * {@link #getActivity} in the back stack.
     *
     * @param context The {@code Context} in which the receiver is running.
     * @param intent  An {@code Intent} containing the channel and data of the current push notification.
     */
    protected void onPushOpen(Context context, Intent intent) {

        // Send a Parse Analytics "push opened" event
        ParseAnalytics.trackAppOpenedInBackground(intent);

        JSONObject pnData = getPushData(intent);
//        resetCount(getNotificationTag(context, pnData));

        String uriString = pnData.optString("uri");
        Intent activityIntent = uriString.isEmpty() ? new Intent(context, getActivity(context, intent))
                : new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));

        activityIntent.putExtras(intent).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

        ParseAnalytics.trackAppOpenedInBackground(intent);

        // allow a urlHash parameter for hash as well as query params.
        // This lets the app know what to do at coldstart by opening a PN.
        // For example: navigate to a specific page of the app
        String urlHash = pnData.optString("urlHash");
        if (urlHash.startsWith("#") || urlHash.startsWith("?")) {
            activityIntent.putExtra("urlHash", urlHash);
        }

        context.startActivity(activityIntent);

        //
        // relay the push notification data to the javascript in case the
        // app is already running when this push is open.
        ParsePushPluginNeu.jsCallback(getPushData(intent), "OPEN");
    }
}
