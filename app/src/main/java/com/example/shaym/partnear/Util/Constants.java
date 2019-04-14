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
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String AVATAR = "avatar";
    public static final String PROFILE_PICTURE = "profile_picture";

    //Intent extras
    public static final String intent_activity_list = "intent_activity_list";
}
