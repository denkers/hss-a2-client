//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.activity;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.activity.ConnectActivity;
import com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.nav.NavigationDrawerFragment;
import com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.nav.SettingsFragment;

public class HomeActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks
{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp
        (
            R.id.navigation_drawer,
            (DrawerLayout) findViewById(R.id.drawer_layout)
        );
    }

    private void showLogout()
    {
        Intent intent   =   new Intent(this, ConnectActivity.class);
        startActivity(intent);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        Fragment fragment   =   null;
        if(position == 3)
        {
            showLogout();
            return;
        }

        switch(position)
        {
            case 0: fragment = new SettingsFragment(); break;
          /*  case 0: fragment = new ProfileFragment(); break;
            case 1: fragment = new SettingsFragment(); break;
            case 2: fragment = new CardsFragment(); break; */
        }

        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
