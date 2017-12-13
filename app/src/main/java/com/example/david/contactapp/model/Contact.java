package com.example.david.contactapp.model;

/**
 * Created by David on 07-Dec-17.
 */

public class Contact {
    private int id;

    private String imagePath;

    private String name;

    private String email;

    private String number;

    public Contact(int id, String name, String email, String image, String number) {
        this.id = id;
        this.imagePath = image;
        this.name = name;
        this.email = email;
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
