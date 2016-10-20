//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import java.io.Serializable;

public class User implements Serializable
{
    //The users display name
    private String name;

    //Profile image bytes
    //If null use the default profile image
    private byte[] profileImage;

    //The users phone number
    private String phoneID;

    //The users email address
    private String email;

    public User()
    {
        name = phoneID = email = "";
    }

    public User(String phoneID)
    {
        this.phoneID    =   phoneID;
    }

    public User(String phoneID, String name)
    {
        this.name           =   name;
        this.phoneID        =   phoneID;
    }

    //Returns the bytes of the profile image
    //Check for null, if so should use default profile image
    public byte[] getProfileImage()
    {
        return profileImage;
    }

    public void setProfileImage(byte[] profileImage)
    {
        this.profileImage = profileImage;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhoneID()
    {
        return phoneID;
    }

    public void setPhoneID(String phoneID)
    {
        this.phoneID = phoneID;
    }

    public void setEmail(String email)
    {
        this.email  =   email;
    }

    public String getEmail()
    {
        return email;
    }

    //Checks if two users are the same by comparing
    //their phone numbers
    @Override
    public boolean equals(Object obj)
    {
        return ((obj instanceof User) && (((User) obj).getPhoneID().equals(phoneID)));
    }

    //Returns the phone numbers hash code
    @Override
    public int hashCode()
    {
        return phoneID.hashCode();
    }
}
