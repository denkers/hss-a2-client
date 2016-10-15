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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.core.User;
import com.kyleruss.hssa2.client.core.UserManager;

import java.util.List;

public class UsersFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private UserListAdapter usersAdapter;

    public UsersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getActivity().getActionBar().setTitle("Users");
        View view   =   inflater.inflate(R.layout.fragment_users, container, false);

        UserManager userManager =   UserManager.getInstance();
        userManager.addUser("12541212", new User("12541212", "kyle"));
        userManager.addUser("12541214", new User("12541214", "joe"));
        userManager.addUser("12541215", new User("12541215", "russell"));

        User[] userList     =   UserManager.getInstance().getUserList().toArray(new User[] {});
        usersAdapter        =   new UserListAdapter(userList, getActivity());
        ListView listView   =   (ListView) view.findViewById(R.id.userList);
        listView.setAdapter(usersAdapter);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Toast.makeText(getActivity(), "user: " + UserManager.getInstance().getUserAt(position).getName(), Toast.LENGTH_SHORT);
    }
}
