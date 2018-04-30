//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.safesms.client.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.kyleruss.safesms.client.core.MessageManager;

public class SMSReceiver extends BroadcastReceiver
{
    //Reads the recieved message and obtains it's sender and message body
    //Forwards the message onto the message manager to handle it
    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            Bundle bundleData   =   intent.getExtras();

            if(bundleData != null)
            {
                Object[] pdus           =   (Object[]) bundleData.get("pdus");
                String messageContent   =   "";
                String sender           =   "";

                //Read in the message
                for(Object pdu : pdus)
                {
                    SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);

                    sender          =   message.getDisplayOriginatingAddress();
                    String msgBody  =   message.getMessageBody();
                    messageContent  +=  msgBody;
                }

                //Pass message to mesage handler to handle the message
                Toast.makeText(context, "New message from: " + sender, Toast.LENGTH_SHORT).show();
                MessageManager.getInstance().handleMessage(sender, messageContent);
            }
        }

        catch(Exception e)
        {
            e.printStackTrace();
            Log.d("SMS_RECV_FAIL", e.getMessage());
        }
    }
}
