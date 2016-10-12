//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserManager
{
    private Map<String, User> onlineUsers;
    private User activeUser;

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
}
