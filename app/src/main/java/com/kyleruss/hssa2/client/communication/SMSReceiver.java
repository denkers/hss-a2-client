//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.kyleruss.hssa2.client.core.MessageManager;

public class SMSReceiver extends BroadcastReceiver
{
    private final SmsManager smsManager = SmsManager.getDefault();

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

                Log.d("pdu_len", "" + pdus.length);

                for(Object pdu : pdus)
                {
                    SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);

                    sender          =   message.getDisplayOriginatingAddress();
                    messageContent  +=  message.getDisplayMessageBody();
                }

                Log.d("RECEIVED_MESSAGE", messageContent);
                MessageManager.getInstance().handleMessage(sender, messageContent);
            }
        }

        catch(Exception e)
        {
            Log.d("SMS_RECV_FAIL", e.getMessage());
        }
    }
}
