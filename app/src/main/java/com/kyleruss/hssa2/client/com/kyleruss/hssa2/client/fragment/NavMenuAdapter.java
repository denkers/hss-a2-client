//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.fragment;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kyleruss.hssa2.client.R;

public class NavMenuAdapter extends ArrayAdapter<String>
{
    private Context context;
    private int layoutResourceId;
    private String[] data;

    public NavMenuAdapter(Context context, int layoutResourceId, String[] data, ListView listView)
    {
        super(context, layoutResourceId, data);
        this.context            =   context;
        this.layoutResourceId   =   layoutResourceId;
        this.data               =   data;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent)
    {
        LayoutInflater inflater =    ((Activity) context).getLayoutInflater();
        View item               =    inflater.inflate(layoutResourceId, parent, false);
        ImageView itemIcon      =    (ImageView) item.findViewById(R.id.rowIcon);
        TextView itemText       =    (TextView) item.findViewById(R.id.rowText);
        int resource            =    -1;

        switch(position)
        {
            case 0: resource = R.drawable.chat_bubbles; break;
            case 1: resource = R.drawable.messages_image; break;
            case 2: resource = R.drawable.usersicon; break;
            case 3: resource = R.drawable.settings_2; break;
            case 4: resource = R.drawable.logout_icon; break;
        }

        if(resource != -1)
            itemIcon.setImageResource(resource);

        itemText.setText(data[position]);

        return item;
    }
}