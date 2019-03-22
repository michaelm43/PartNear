package com.example.shaym.partnear.Logic;

import android.os.Parcelable;

import java.util.Date;
import android.os.Parcel;
import android.os.Parcelable;

public class Activity implements Parcelable {
    public String eventName,event_time,event_date,user_id;

        public Activity() { }

        public Activity(String eventName, String event_time, String event_date, String user_id) {
            this.eventName = eventName;
            this.event_time = event_time;
            this.event_date = event_date;
            this.user_id = user_id;
        }

    protected Activity(Parcel in) {
        eventName = in.readString();
        event_time = in.readString();
        event_date = in.readString();
        user_id = in.readString();
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
    }





 /*   private String activityName;
    private String activityID;
    private Upload manager;
    private User[] participants;

    private int slots;
    private String startTime;       //TODO change to Date?
    private Date endTime;
    private String location;

    public Activity(String activityName, Upload manager, String startTime) {
        this.activityName = activityName;
//        this.activityID = activityID;
        this.manager = manager;
        this.participants = new User[slots];
//        this.slots = slots;
        this.startTime = startTime;
//        this.location = location;
    }

//    public Activity(String activityName) {
//        this.activityName = activityName;
//    }

    protected Activity(Parcel in) {
        this.activityName = in.readString();
        this.activityID = in.readString();
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

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getActivityName() {
        return activityName;
    }

    public Upload getManager() {
        return manager;
    }

    public User[] getParticipants() {
        return participants;
    }

    public int getSlots() {
        return slots;
    }

    public String getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public boolean addParticipant(User user) {
        if(this.slots > 0)
        {
            this.participants[this.participants.length-this.slots] = user;
            return true;
        }
        return false;
    }

    public boolean removeParticipant(User user) {
        for (int i = 0; i < this.participants.length; i++){
            if(this.participants[i].getUserID() == user.getUserID())
            {
                this.participants[i] = null;
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "title='" + this.activityName + '\'' +
                ", activity_id='" + this.activityID + '\'' +
                '}';
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.activityName);
        dest.writeString(this.activityID);
    }*/
}