package com.example.shaym.partnear.Logic;

import android.os.Parcelable;

import java.util.Date;
import android.os.Parcel;

import com.example.shaym.partnear.R;
import com.google.firebase.firestore.GeoPoint;

public class Activity implements Parcelable {
    public String eventName,event_time,event_date,user_id;
    public GeoPoint location;
    public int eventType, image_id;

        public Activity() { }

        public Activity(String eventName, String event_time, String event_date, String user_id, GeoPoint location, int eventType) {
            this.eventName = eventName;
            this.event_time = event_time;
            this.event_date = event_date;
            this.user_id = user_id;
            this.location = location;
            this.eventType = eventType;
        }

    protected Activity(Parcel in) {
        eventName = in.readString();
        event_time = in.readString();
        event_date = in.readString();
        user_id = in.readString();
        Double lat = in.readDouble();
        Double lng = in.readDouble();
        location = new GeoPoint(lat, lng);
        eventType = in.readInt();
    }

    public static final Creator<Activity> CREATOR = new Creator<Activity>() {
        @Override
        public Activity createFromParcel(Parcel in) {
            return new Activity(in);
        }

        @Override
        public Activity[] newArray(int size) {
            return new Activity[size];
        }
    };

    public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public String getEvent_time() {
            return event_time;
        }

        public void setEvent_time(String event_time) {
            this.event_time = event_time;
        }

        public String getEvent_date() {
            return event_date;
        }

        public void setEvent_date(String event_date) {
            this.event_date = event_date;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

    public int getEventType() {
        return eventType;
    }

    public int getImage_id() {
        switch(eventType){
            case 0:{
                image_id = R.drawable.football_image;
                break;
            }
            case 1:{
                image_id = R.drawable.basketball_image;
                break;
            }
            case 2:{
                image_id = R.drawable.dance_image;
                break;
            }
            case 3:{
                image_id = R.drawable.sing_image;
                break;
            }
            case 4:{
                image_id = R.drawable.study_image;
                break;
            }
            case 5:{
                image_id = R.drawable.gaming_image;
                break;
            }
        }
        return image_id;
    }

    public GeoPoint getLocation() {
        return location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(eventName);
        parcel.writeString(event_time);
        parcel.writeString(event_date);
        parcel.writeString(user_id);
        parcel.writeDouble(location.getLatitude());
        parcel.writeDouble(location.getLongitude());
        parcel.writeInt(eventType);
    }

}