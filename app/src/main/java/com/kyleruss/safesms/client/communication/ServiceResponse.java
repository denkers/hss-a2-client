//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.safesms.client.communication;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.kyleruss.safesms.client.R;

public class ServiceResponse
{
    //An indicator to whether the service/action was successful
    private boolean status;

    //A message/description of the response i.e error message
    private String message;

    //Some extra data to pass back
    private JsonObject data;

    //An indicator to whether this was a information response not success/fail
    //Mostly used for ServiceResponse@showToastResponse
    private boolean info;

    public ServiceResponse()
    {
        this("", false);
    }

    public ServiceResponse(String message, boolean status)
    {
        this.message    =   message;
        this.status     =   status;
        info            =   false;
    }

    public ServiceResponse setInfo(boolean info)
    {
        this.info   =   info;
        return this;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean getStatus()
    {
        return status;
    }

    public void setMessage(String message)
    {
        this.message    =   message;
    }

    public void setStatus(boolean status)
    {
        this.status =   status;
    }

    public JsonObject getData()
    {
        return data;
    }

    public void setData(JsonObject data)
    {
        this.data   =   data;
    }

    //Displays a custom toast message based on the response fields
    public void showToastResponse(Activity activity)
    {
        LayoutInflater inflater =   activity.getLayoutInflater();
        View layout             =   inflater.inflate(R.layout.toast_layout, (ViewGroup) activity.findViewById(R.id.stoast_layout));
        TextView textView       =   (TextView) layout.findViewById(R.id.stoast_text);
        textView.setText(message);

        ImageView imageView     =   (ImageView) layout.findViewById(R.id.stoast_icon);

        //Set the toast drawable based on if it is an info message or success/fail status
        if(info)
            imageView.setImageResource(R.drawable.info);
        else
            imageView.setImageResource(status? R.drawable.successicon : R.drawable.failicon);

        Toast toast =   new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
