//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.nav;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.core.User;

public class UserListAdapter extends ArrayAdapter<User>
{
    private final Activity context;
    private User[] users;

    public UserListAdapter(User[] users, Activity context)
    {
        super(context, R.layout.user_list_row, users);
        this.users      =   users;
        this.context    =   context;
    }

    public View getView(int position, View view, ViewGroup parent)
    {
        LayoutInflater inflater =   context.getLayoutInflater();
        View viewRow            =   inflater.inflate(R.layout.user_list_row, null, true);
        TextView nameView       =   (TextView) viewRow.findViewById(R.id.nameView);
        ImageView profImageView =   (ImageView) viewRow.findViewById(R.id.profImageView);
        User user               =   users[position];

        nameView.setText(user.getName() + "\n" + user.getPhoneID());

        profImageView.setImageResource(R.drawable.default_profile);
        return viewRow;
    }
}
