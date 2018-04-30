//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.safesms.client.core;

import android.telephony.SmsManager;
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

    //A list of current messages the client has received
    private List<Message> messages;

    private MessageManager()
    {
        messages    =   new ArrayList<>();
    }

    //Pushes the message to the front of the list
    public void addMessage(Message message)
    {
        messages.add(0, message);
    }

    public List<Message> getMessages()
    {
        return messages;
    }

    //Handles a received message (sender, message body)
    //Checks if a session key exists for the user to decrypt message otherwise it
    //decrypts the message with the clients private key where the message was encrypted with the clients public key
    //The decrypted message is then the session key to be used in future
    //If the session key is found, decrypt the message content with AES using the session key
    public void handleMessage(String fromID, String content)
    {
        try
        {
            KeyManager keyManager = KeyManager.getInstance();
            SecretKeySpec secretKey = keyManager.getSessionKey(fromID);

            //Session key not found, decrypt message content with client private key
            if (secretKey == null)
            {
                //base 64 decode the message body
                byte[] decodedContent       =   Base64.decode(content, Base64.DEFAULT);

                //decrypt the RSA encrypted message with the clients private key
                //The resulting plain text is a 128bit AES session key
                PrivateKey clientPrivate    =   keyManager.getClientPrivateKey();
                byte[] decryptedContent     =   CryptoCommons.publicDecryptBytes(decodedContent, clientPrivate);
                keyManager.setUserSessionKey(fromID, decryptedContent);
            }

            //Session key found, add the message to the message list
            //User can then decrypt the message later when they want using the session key
            //See MessageManager@decryptMessage
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

    //Decrypts the passed message with AES using the respective session key
    //Message content is mutated with decrypted content and message is set to decrypted status
    //Returns true if the message was successfully decrypted; false otherwise
    public boolean decryptMessage(Message message)
    {
        if(message == null) return false;

        else
        {
            try
            {
                //Get the session key for the message sender
                //Session key should exist, if not return false
                String from = message.getFrom();
                KeyManager keyManager   =   KeyManager.getInstance();
                SecretKeySpec secretKey =   keyManager.getSessionKey(from);

                if (secretKey != null)
                {
                    //Base 64 decode the message body
                    byte[] decodedContent = Base64.decode(message.getContent(), Base64.DEFAULT);

                    //decrypt the decoded message body with AES using the session key for the sender
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

    //Sends an SMS message with byte data which is base 64 encoded
    //Make sure to base 64 decode on receiving the message
    //See MessageManager@sendMessage(String, String)
    public void sendEncodedMessage(String toID, byte[] data)
    {
        String encodedContent = Base64.encodeToString(data, Base64.NO_WRAP);
        sendMessage(toID, encodedContent);
    }

    //Sends an SMS message to the receiver @toID with the passed @content
    //If message content length is less than 160 chars, send one complete sms
    //Otherwise send a multipart sms message
    //Phone vendors are expected to handle multipart sms messages in such a way that
    //On recieving the multipart message, it will appear as one whole sms message
    public void sendMessage(String toID, String content)
    {
        SmsManager smsManager =     SmsManager.getDefault();

        if(content.length() <= 160)
            smsManager.sendTextMessage(toID, null, content, null, null);
        else
        {
            ArrayList<String> parts     =   smsManager.divideMessage(content);
            smsManager.sendMultipartTextMessage(toID, null, parts, null, null);
        }
    }

    public static MessageManager getInstance()
    {
        if(instance == null) instance = new MessageManager();
        return instance;
    }
}
