//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.kyleruss.hssa2.commons.CryptoCommons;

import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

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

    public void handleMessage(String fromID, String content)
    {
        Log.d("HANDLE_MESSAGE", "handling message: " + content.length());
        try
        {
            KeyManager keyManager = KeyManager.getInstance();
            SecretKeySpec secretKey = keyManager.getSessionKey(fromID);

            if (secretKey == null)
            {
                byte[] decodedContent       =   Base64.decode(content, Base64.DEFAULT);
                PrivateKey clientPrivate    =   keyManager.getClientPrivateKey();
                byte[] decryptedContent     =   CryptoCommons.publicDecryptBytes(decodedContent, clientPrivate);
                keyManager.setUserSessionKey(fromID, decryptedContent);
            }

            else
            {
                Message message =   new Message(fromID, content);
                addMessage(message);
            }
        }

        catch(Exception e)
        {
            e.printStackTrace();
            Log.d("MESSAGE_HANDLE_FAIL", e.getMessage());
        }
    }

    public boolean decryptMessage(Message message)
    {
        if(message == null) return false;

        else
        {
            try
            {
                String from = message.getFrom();
                KeyManager keyManager   =   KeyManager.getInstance();
                SecretKeySpec secretKey =   keyManager.getSessionKey(from);

                if (secretKey != null)
                {
                    byte[] decodedContent = Base64.decode(message.getContent(), Base64.DEFAULT);
                    String decryptedContent =   new String(CryptoCommons.AES(secretKey, decodedContent, false));
                    message.setContent(decryptedContent);
                    message.setDecrypted(true);
                    return true;
                }

                else return false;

            }

            catch(Exception e)
            {
                Log.d("MESSAGE_DECR_FAIL", e.getMessage());
                return false;
            }
        }
    }

    public void sendEncodedMessage(String toID, byte[] data)
    {
        String encodedContent = Base64.encodeToString(data, Base64.NO_WRAP);
        Log.d("SEND_ENCODED", "SENDING: " + encodedContent);
        sendMessage(toID, encodedContent);
    }

    public void sendMessage(String toID, String content)
    {
        SmsManager smsManager =     SmsManager.getDefault();

        if(content.length() <= 160)
            smsManager.sendTextMessage(toID, null, content, null, null);
        else
        {
            ArrayList<String> parts =   smsManager.divideMessage(content);
            Log.d("SEND_MESSAGE", "sending long message: " + content);
            //for(String part : parts)
            smsManager.sendMultipartTextMessage(toID, null, parts, null, null);
        }
    }

    public void readMessage(int index)
    {
        Message message =   messages.get(index);
        if(message != null) message.setRead(true);
    }

    public static MessageManager getInstance()
    {
        if(instance == null) instance = new MessageManager();
        return instance;
    }
}
