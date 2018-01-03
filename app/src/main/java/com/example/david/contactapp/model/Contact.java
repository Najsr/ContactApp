package com.example.david.contactapp.model;

import android.widget.ImageView;
import android.widget.TextView;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by David on 07-Dec-17.
 */

public class Contact extends RealmObject {
    @PrimaryKey
    private int id;

    private String imagePath;

    private String name;

    private String email;

    private String number;

    /*
    public Contact(int id, String name, String email, String image, String number) {
        this.id = id;
        this.imagePath = image;
        this.name = name;
        this.email = email;
        this.number = number;
    }
    */

    @Override
    public String toString() {
        return this.getName();
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getId() {
        return id;
    }

    public String getImagePath(){
        return imagePath;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public String getNumber() {
        return number;
    }

}
