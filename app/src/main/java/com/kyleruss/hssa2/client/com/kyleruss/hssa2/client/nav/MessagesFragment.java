//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.nav;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kyleruss.hssa2.client.R;


public class MessagesFragment extends Fragment
{
    public MessagesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getActivity().getActionBar().setTitle("Messages");
        View view   =    inflater.inflate(R.layout.fragment_messages, container, false);
        return view;
    }
}
