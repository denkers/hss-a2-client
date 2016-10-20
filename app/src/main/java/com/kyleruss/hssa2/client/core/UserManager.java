//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

    //The users that are currently active/online
    //Key: the users phone number, value: the corresponding user object
    private Map<String, User> onlineUsers;

    //The user object for the current client
    private User activeUser;

    private UserManager()
    {
        onlineUsers =   new LinkedHashMap<>();
    }

    //Stores the passed phone number in the phones storage
    //Storage key: phoneID
    public void savePhoneID(String phoneID, Activity activity)
    {
        SharedPreferences sharedPreferences =   PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor prefEditor =   sharedPreferences.edit();
        prefEditor.putString("phoneID", phoneID);

        prefEditor.commit();
    }

    //Retrieves the clients phone number from storage
    //Using the storage key: phoneID
    public String getPhoneID(Activity activity)
    {
        SharedPreferences sharedPreferences =   PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPreferences.getString("phoneID", null);
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

    public static UserManager getInstance()
    {
        if(instance == null) instance = new UserManager();
        return instance;
    }
}
