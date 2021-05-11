package com.imagesharer.models;

public class Upload {
    private String mName;
    private String mImageUrl;

    public Upload(String mName, String mImageUrl) {
        if (mName.trim().equals("")) {
            mName = "No Name";
        }
        this.mName = mName;
        this.mImageUrl = mImageUrl;
    }
    public Upload(){

    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }
    public String getImageUrl() {
        return mImageUrl;
    }
    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }
}
