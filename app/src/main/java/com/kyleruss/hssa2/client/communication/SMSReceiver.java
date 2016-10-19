//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.communication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.kyleruss.hssa2.client.core.MessageManager;

import java.util.HashMap;
import java.util.Map;

public class SMSReceiver extends BroadcastReceiver
{
    private final SmsManager smsManager         =   SmsManager.getDefault();

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


                for(int i = 0; i < pdus.length; i++)
                {
                    SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[i]);

                    sender          =   message.getDisplayOriginatingAddress();
                    String msgBody  =   message.getMessageBody();
                    messageContent  += msgBody;
                }

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
