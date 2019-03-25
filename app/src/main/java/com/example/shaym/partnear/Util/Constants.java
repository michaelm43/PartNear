package com.example.shaym.partnear.Util;

import android.content.Intent;

public class Constants {
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;

    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static final String ACTIVITY_KEY = "activity";

    //Firestore Strings
    public static final String collection_activities = "Activities";
    public static final String collection_users = "users";

    //Intent extras
    public static final String intent_activity_list = "intent_activity_list";
}
