//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.kyleruss.hssa2.commons.CryptoCommons;

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
        try
        {
            KeyManager keyManager = KeyManager.getInstance();
            SecretKeySpec secretKey = keyManager.getSessionKey(fromID);

            if (secretKey == null)
            {
                byte[] decodedContent = Base64.decode(content, Base64.DEFAULT);
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
                KeyManager keyManager = KeyManager.getInstance();
                SecretKeySpec secretKey = keyManager.getSessionKey(from);
                byte[] decodedContent = Base64.decode(message.getContent(), Base64.DEFAULT);

                if (secretKey != null)
                {
                    String decryptedContent =   new String(CryptoCommons.AES(secretKey, decodedContent, false));
                    message.setContent(decryptedContent);
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
        String encodedContent   =   Base64.encodeToString(data, Base64.NO_WRAP);
        sendMessage(toID, encodedContent);
    }

    public void sendMessage(String toID, String content)
    {
        SmsManager smsManager =     SmsManager.getDefault();
        smsManager.sendTextMessage(toID, null, content, null, null);
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
