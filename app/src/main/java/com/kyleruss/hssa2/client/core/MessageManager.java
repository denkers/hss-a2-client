//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.core;

import android.util.Base64;
import android.util.Log;

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

                if (secretKey == null)
                {
                    PrivateKey clientPrivate = keyManager.getClientPrivateKey();
                    byte[] decryptedContent = CryptoCommons.publicDecryptBytes(decodedContent, clientPrivate);
                    keyManager.setUserSessionKey(from, decryptedContent);
                    return true;
                }

                else
                {
                    String decryptedContent =   new String(CryptoCommons.AESDecrypt(secretKey, decodedContent));
                    message.setContent(decryptedContent);
                    return true;
                }
            }

            catch(Exception e)
            {
                Log.d("MESSAGE_DECR_FAIL", e.getMessage());
                return false;
            }
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
