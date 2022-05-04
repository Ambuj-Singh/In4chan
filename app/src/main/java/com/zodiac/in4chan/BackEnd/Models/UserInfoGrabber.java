package com.zodiac.in4chan.BackEnd.Models;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.List;


@IgnoreExtraProperties
public class UserInfoGrabber {

    private String username;
    private int Age;
    private String image;
    private Boolean UserStatus;
    private String UID;
    private String name;


    public UserInfoGrabber(){

    }

    public UserInfoGrabber(String username, int Age, String image, Boolean UserStatus, String UID, String name) {
        this.username = username;
        this.Age = Age;
        this.image = image;
        this.UserStatus = UserStatus;
        this.UID = UID;
        this.name = name;
    }

    public String getusername() {
        return username;
    }

    public void setusername(String username) {
        this.username = username;
    }

    public int getAge() {
        return Age;
    }

    public void setAge(int Age) {
        this.Age = Age;
    }

    public Boolean getUserStatus() {
        return UserStatus;
    }

    public void setUserStatus(Boolean userStatus) {
        UserStatus = userStatus;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}