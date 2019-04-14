package com.example.shaym.partnear.Logic;

public class User {
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private String date;
    private String gender;

    public User(){
        //empty c'tor
    }

    public User(String name,String email,String phone,String date,String gender){
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.date = date;
        this.gender = gender;
        this.avatar = "default";
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getAvatar(){
        return this.avatar;
    }

    public void setAvatar(String avatar ){
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
