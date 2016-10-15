//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.nav;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kyleruss.hssa2.client.R;


public class SendSMSFragment extends Fragment
{
    public SendSMSFragment () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getActivity().getActionBar().setTitle("Send SMS");
        View view   =   inflater.inflate(R.layout.fragment_send_sms, container, false);
        return view;
    }
}
