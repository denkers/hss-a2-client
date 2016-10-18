//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserManager
{
    private static UserManager instance;
    private Map<String, User> onlineUsers;
    private User activeUser;

    private UserManager()
    {
        onlineUsers =   new LinkedHashMap<>();
    }

    public User getActiveUser()
    {
        return activeUser;
    }

    public void setActiveUser(User activeUser)
    {
        this.activeUser =   activeUser;
    }

    public boolean hasActiveUser()
    {
        return activeUser   != null;
    }

    public Collection<String> getPhoneIDList()
    {
        return onlineUsers.keySet();
    }

    public Collection<User> getUserList()
    {
        return onlineUsers.values();
    }

    public User getUser(String phoneID)
    {
        return onlineUsers.get(phoneID);
    }

    public User getUserAt(int index)
    {
        return getUser((String) getPhoneIDList().toArray()[index]);
    }

    public void clearUsers()
    {
        onlineUsers.clear();
    }

    public void addUser(String phoneID, User user)
    {
        onlineUsers.put(phoneID, user);
    }

    public void removeUser(String phoneID)
    {
        onlineUsers.remove(phoneID);
    }

    public int getOnlineUserCount()
    {
        return onlineUsers.size();
    }

    public void resetOnlineUsers()
    {
        onlineUsers.clear();
    }

    public String getPhoneID(Activity activity)
    {
        TelephonyManager telephoneManager   =   (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber                  =   telephoneManager.getLine1Number();

        return phoneNumber;
    }

    public static UserManager getInstance()
    {
        if(instance == null) instance = new UserManager();
        return instance;
    }
}
