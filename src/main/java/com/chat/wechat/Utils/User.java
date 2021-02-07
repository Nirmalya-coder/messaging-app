package com.chat.wechat.Utils;

public class User {

    private String name, Uid, phoneNumber, profileImage;

    public User()
    {

    }

    public User(String uid, String name, String phoneNumber, String profileImage) {
        this.name = name;
        this.Uid = uid;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return Uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

}
