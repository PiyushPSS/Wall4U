package com.lithium.wall4u.PersonalProfile;

import androidx.annotation.Keep;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

@Keep
public class UserModel {

    String fullName, email;

    @ServerTimestamp
    Timestamp dateCreated;

    public UserModel(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    public UserModel() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }
}
