//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable
{
    private String from;
    private String content;
    private boolean read;
    private Date timeRecv;

    public Message(String from, String content)
    {
        this.from       =   from;
        this.content    =   content;
        this.read       =   false;
        this.timeRecv   =   new Date();
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Date getTimeRecv()
    {
        return timeRecv;
    }

    public void setTimeRecv(Date timeRecv)
    {
        this.timeRecv = timeRecv;
    }

    public boolean isRead()
    {
        return read;
    }

    public void setRead(boolean read)
    {
        this.read = read;
    }
}
