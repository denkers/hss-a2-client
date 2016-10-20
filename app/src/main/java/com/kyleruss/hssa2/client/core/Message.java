//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import android.util.Base64;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Cipher;

public class Message implements Serializable
{
    //The senders phone number
    private String from;

    //The message body
    private String content;

    //True if the message has been opened/read by the client; otherwise false
    private boolean read;

    //The date/time the message was received by the client
    private Date timeRecv;

    //True if the message has already been decrypted; false otherwise
    private boolean decrypted;

    public Message(String from, String content)
    {
        this.from       =   from;
        this.content    =   content;
        this.read       =   false;
        this.timeRecv   =   new Date();
        this.decrypted  =   false;
    }

    public boolean isDecrypted()
    {
        return decrypted;
    }

    public void setDecrypted(boolean decrypted)
    {
        this.decrypted  =   decrypted;
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

    public String recvDateToString()
    {
        return new SimpleDateFormat("E, h:m a").format(timeRecv);
    }
}
