package com.example.shaym.partnear.Logic;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String name;
    private String email;
    private String userID;
    private String phone;
    private String avatar;

    public User(String name, String email, String userID, String phone, String avatar) {
        this.name = name;
        this.email = email;
        this.userID = userID;
        this.phone = phone;
        this.avatar = avatar;
    }

    protected User(Parcel in) {
        name = in.readString();
        email = in.readString();
        userID = in.readString();
        phone = in.readString();
        avatar = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUserID() {
        return userID;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(userID);
        dest.writeString(phone);
        dest.writeString(avatar);
    }
}
