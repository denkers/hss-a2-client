//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

public class User
{
    private String name;
    private byte[] profileImage;
    private String phoneID;
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

    @Override
    public boolean equals(Object obj)
    {
        return ((obj instanceof User) && (((User) obj).getPhoneID().equals(phoneID)));
    }

    @Override
    public int hashCode()
    {
        return phoneID.hashCode();
    }
}
