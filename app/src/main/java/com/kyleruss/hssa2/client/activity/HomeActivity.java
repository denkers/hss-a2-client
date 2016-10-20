//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.activity;

import android.app.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.fragment.MessagesFragment;
import com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.fragment.NavigationDrawerFragment;
import com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.fragment.SendSMSFragment;
import com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.fragment.SettingsFragment;
import com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.fragment.UsersFragment;
import com.kyleruss.hssa2.client.communication.CommUtils;
import com.kyleruss.hssa2.client.communication.HTTPAsync;
import com.kyleruss.hssa2.client.communication.ServiceRequest;
import com.kyleruss.hssa2.client.communication.ServiceResponse;
import com.kyleruss.hssa2.client.core.ClientConfig;
import com.kyleruss.hssa2.client.core.KeyManager;
import com.kyleruss.hssa2.client.core.UserManager;
import com.kyleruss.hssa2.commons.EncryptedSession;
import com.kyleruss.hssa2.commons.RequestPaths;

//-----------------------------------------
//  HomeActivity
//-----------------------------------------
//Layout: R.layout.activity_home
//About: Central app hub/navigation

public class HomeActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks
{

    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mNavigationDrawerFragment.setUp
        (
            R.id.navigation_drawer,
            (DrawerLayout) findViewById(R.id.drawer_layout)
        );
    }

    //Redirects the user to connect view
    private void showLogout()
    {
        Intent intent   =   new Intent(this, ConnectActivity.class);
        startActivity(intent);
    }

    //Sends a request to disconnect from the server
    //Server will remove user from online users etc.
    //On success redirect user to connect view
    private void logoutUser()
    {
        JsonObject requestObj   =   new JsonObject();
        String phoneID          =   UserManager.getInstance().getActiveUser().getPhoneID();
        requestObj.addProperty("phoneID", phoneID);

        try
        {
            EncryptedSession encSession =   new EncryptedSession(requestObj.toString().getBytes("UTF-8"), KeyManager.getInstance().getServerPublicKey());
            ServiceRequest request      =   CommUtils.prepareEncryptedSessionRequest(encSession);
            request.setURL(ClientConfig.CONN_URL + RequestPaths.SERV_DISCON_REQ);
            request.setGet(false);

            LogoutTask task     =   new LogoutTask();
            task.execute(request);
        }

        catch (Exception e)
        {
            new ServiceResponse("Failed to logout", false).showToastResponse(HomeActivity.this);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        Fragment fragment   =   null;
        if(position == 4)
        {
            logoutUser();
            return;
        }

        switch(position)
        {
            case 0: fragment = new SendSMSFragment(); break;
            case 1: fragment = new MessagesFragment(); break;
            case 2: fragment = new UsersFragment(); break;
            case 3: fragment = new SettingsFragment(); break;
        }

        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    //Response handler for logging out request
    //If disconnect was successful, clean up users, key instances etc.
    //Redirect user back to connect view
    private class LogoutTask extends HTTPAsync
    {
        @Override
        protected void onPostExecute(String response)
        {
            JsonObject responseObj  =   CommUtils.parseJsonInput(response);

            if(responseObj.get("actionStatus").getAsBoolean())
            {
                UserManager.getInstance().setActiveUser(null);
                UserManager.getInstance().clearUsers();
                KeyManager.getInstance().resetKeys();

                new ServiceResponse("Successfully logged out", true).showToastResponse(HomeActivity.this);
                showLogout();
            }

            else new ServiceResponse("Failed to logout", false).showToastResponse(HomeActivity.this);
        }
    }
}
