//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.communication;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.kyleruss.hssa2.client.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public abstract class HTTPAsync extends AsyncTask<ServiceRequest, Void, String>
{
    private Dialog processDialog;

    private String getResponse(HttpURLConnection conn) throws IOException
    {
        String response =   "";
        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;

            while ((line = reader.readLine()) != null)
                response += line + "\n";

            inputStream.close();
            return response;
        }

        else return "fail: " + conn.getResponseCode();
    }

    private void writeRequest(HttpURLConnection conn, String params) throws IOException
    {
        OutputStreamWriter writer   =   new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
        writer.write(params);
        writer.flush();
        writer.close();
    }

    protected ServiceResponse getServiceResponse(String response)
    {
        JsonObject jsonObject   =   CommUtils.parseJsonInput(response);
        boolean status          =   jsonObject.get("status").getAsBoolean();
        String message          =   jsonObject.get("message").getAsString();
        ServiceResponse serviceResponse         =   new ServiceResponse(message, status);
        return serviceResponse;
    }

    public void showServicingSpinner(Context context)
    {
        showServicingSpinner(context, "Please wait");
    }

    public void showServicingSpinner(Context context, String message)
    {
        /*v.setImageResource(android.R.color.transparent);
        v.setBackgroundResource(R.drawable.spinner_animation);
        AnimationDrawable animation =   (AnimationDrawable) v.getBackground();
        animation.start(); */

        processDialog   =   new Dialog(context);
        processDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        processDialog.setContentView(R.layout.process_dialog);

        ((TextView) processDialog.findViewById(R.id.processDialogText)).setText(message);
        AnimationDrawable animation =   (AnimationDrawable) (processDialog.findViewById(R.id.processSpinner)).getBackground();
        animation.start();
        processDialog.show();
    }

    public void hideServicingSpinner()
    {
        if(processDialog == null) return;

        AnimationDrawable animation =   (AnimationDrawable) (processDialog.findViewById(R.id.processSpinner).getBackground());
        animation.stop();
        processDialog.dismiss();
        //v.setBackgroundResource(android.R.color.transparent);
        //v.setImageResource(prevDrawable);
    }

    @Override
    protected String doInBackground(ServiceRequest... requests)
    {
        try
        {
            ServiceRequest request          =   requests[0];
            HttpURLConnection connection    =   request.getConnection();

            if(!request.isGet())
                writeRequest(connection, request.prepareParams());

            return getResponse(connection);
        }

        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected abstract void onPostExecute(String response);
}
