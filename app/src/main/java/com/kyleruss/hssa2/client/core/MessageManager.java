//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import java.util.ArrayList;
import java.util.List;

public class MessageManager
{
    private static MessageManager instance;
    private List<Message> messages;

    private MessageManager()
    {
        messages    =   new ArrayList<>();
    }

    public void addMessage(Message message)
    {
        messages.add(0, message);
    }

    public Message removeMessage(int index)
    {
        return messages.remove(index);
    }

    public List<Message> getMessages()
    {
        return messages;
    }

    public static MessageManager getInstance()
    {
        if(instance == null) instance = new MessageManager();
        return instance;
    }
}
