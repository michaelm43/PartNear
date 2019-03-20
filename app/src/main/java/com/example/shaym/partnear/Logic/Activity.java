package com.example.shaym.partnear.Logic;

import android.os.Parcelable;

import java.util.Date;
import android.os.Parcel;
import android.os.Parcelable;

public class Activity implements Parcelable {
    private String activityName;
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

    public Activity(String activityName) {
        this.activityName = activityName;
    }

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
    }
}