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
    private String from;
    private String content;
    private boolean read;
    private Date timeRecv;
    private boolean decrypted;

    public Message(String from, String content)
    {
        this.from       =   from;
        this.content    =   content;
        this.read       =   false;
        this.timeRecv   =   new Date();
        this.decrypted  =   false;
    }

    public boolean decryptMessage(Cipher cipher, boolean decode)
    {
        try
        {
            byte[] contentData      =   decode ? Base64.decode(content, Base64.DEFAULT) : content.getBytes("UTF-8");
            byte[] decryptedData    =   cipher.doFinal(contentData);
            content                 =   new String(decryptedData);
            return true;
        }

        catch(Exception e)
        {
            Log.d("MESSAGE_DEC_FAIL", e.getMessage());
            return false;
        }
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
