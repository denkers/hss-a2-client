//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

public class User
{
    private String name;
    private String profileImage;
    private String phoneID;

    public String getProfileImage()
    {
        return profileImage;
    }

    public void setProfileImage(String profileImage)
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
