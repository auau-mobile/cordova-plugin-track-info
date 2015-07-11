/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package com.paranoidfrog.trackinfo;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class CurrentlyPlaying extends CordovaPlugin {

    private CallbackContext musicCallbackContext;
    BroadcastReceiver receiver;
    private String lastMusic = "Unknown artist";

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.musicCallbackContext = null;

        // We need to listen to connectivity events to update navigator.connection
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.music.metachanged");
        intentFilter.addAction("com.android.music.playstatechanged");
        intentFilter.addAction("com.android.music.playbackcomplete");
        intentFilter.addAction("com.android.music.queuechanged");

        if (this.receiver == null) {
            this.receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                String artist = intent.getStringExtra("artist");
                String album = intent.getStringExtra("album");
                String track = intent.getStringExtra("track");
                String musicInfo = artist + " - " + track;

                    // (The null check is for the ARM Emulator, please use Intel Emulator for better results)
                    if(TrackInfo.this.webView != null)
                        sendUpdate(musicInfo);
                }
            };
            webView.getContext().registerReceiver(this.receiver, intentFilter);
        }

    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (action.equals("getMusicInfo")) {
            this.musicCallbackContext = callbackContext;

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, lastMusic);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            
            return true;
        }
        return false;
    }

    /**
     * Stop music receiver.
     */
    public void onDestroy() {
        if (this.receiver != null) {
            try {
                webView.getContext().unregisterReceiver(this.receiver);
            } catch (Exception e) {
                Log.e("Error", "Error unregistering music receiver: " + e.getMessage(), e);
            } finally {
                receiver = null;
            }
        }
    }

    /**
     * Create a new plugin result and send it back to JavaScript
     *
     * @param 
     */
    private void sendUpdate(String music) {
        if (musicCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, music);
            result.setKeepCallback(true);
            musicCallbackContext.sendPluginResult(result);
        }
        webView.postMessage("currentplaying", music);
        
        lastMusic = music;
    }

}