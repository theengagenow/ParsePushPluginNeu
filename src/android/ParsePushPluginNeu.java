package com.theengagenow.ParsePushPluginNeu;

/**
 * Created by Engagenow Data Sciences Pvt. Ltd. 19/04/2021.
 */

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ParsePushPluginNeu extends CordovaPlugin {
    private static final String ACTION_REGISTER_CALLBACK = "registerCallback";;

    private static CallbackContext gEventCallback = null;
    private static Queue<PluginResult> pnQueue = new LinkedList();

    private static CordovaWebView gWebView;
    private static boolean gForeground = false;
    private static boolean helperPause = false;

    public static final String LOGTAG = "com.theengagenow.ParsePushPluginNeu";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_REGISTER_CALLBACK)) {
            gEventCallback = callbackContext;

            if (!pnQueue.isEmpty())  {
                flushPNQueue();
            }
            return true;
        }
        return false;
    }



    /*
     * keep reusing the saved callback context to call the javascript PN handler
     */
    public static void jsCallback(JSONObject _json) {
        jsCallback(_json, "RECEIVE");
    }

    public static void jsCallback(JSONObject _json, String pushAction) {
        List<PluginResult> cbParams = new ArrayList<PluginResult>();
        cbParams.add(new PluginResult(PluginResult.Status.OK, _json));
        cbParams.add(new PluginResult(PluginResult.Status.OK, pushAction));
        //avoid blank
        PluginResult dataResult;
        if (pushAction.equals("OPEN")) {
            dataResult = new PluginResult(PluginResult.Status.OK, cbParams);
        } else {
            dataResult = new PluginResult(PluginResult.Status.OK, _json);
        }
        dataResult.setKeepCallback(true);

        if (gEventCallback != null) {
            gEventCallback.sendPluginResult(dataResult);
        } else {
            //
            // save the incoming push payloads until gEventCallback is ready.
            // put a sensible limit on how queue size;
            if (pnQueue.size() < 10) {
                //pnQueue.add(new PNQueueItem(_json, pushAction));
                pnQueue.add(dataResult);
            }
        }
    }

    private static void flushPNQueue() {
        while (!pnQueue.isEmpty() && gEventCallback != null) {
            gEventCallback.sendPluginResult(pnQueue.remove());
        }
    }

    @Override
    protected void pluginInitialize() {
        gWebView = this.webView;
        gForeground = true;
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        gForeground = false;
        helperPause = true;
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        gForeground = true;
    }

    @Override
    public void onDestroy() {
        gWebView = null;
        gForeground = false;
        gEventCallback = null;
        helperPause = false;

        super.onDestroy();
    }

    public static boolean isInForeground() {
        return gForeground;
    }
}
