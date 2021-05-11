package com.imagesharer.models;

public class ModelUser {
    String uid, name, email, image, search;


    public ModelUser(){

    }

    public ModelUser(String uid, String name, String email, String image, String search) {

        this.uid = uid;
        this.name = name;
        this.email = email;
        this.image = image;
        this.search = search;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}