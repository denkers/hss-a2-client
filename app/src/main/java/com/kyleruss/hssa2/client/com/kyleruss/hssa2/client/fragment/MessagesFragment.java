//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.hssa2.client.com.kyleruss.hssa2.client.fragment;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.kyleruss.hssa2.client.R;
import com.kyleruss.hssa2.client.core.Message;
import com.kyleruss.hssa2.client.core.MessageListAdapter;
import com.kyleruss.hssa2.client.core.MessageManager;
import com.kyleruss.hssa2.client.core.User;

import java.util.List;


public class MessagesFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private MessageListAdapter messageAdapter;

    public MessagesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //getActivity().getActionBar().setTitle("Messages");
        View view   =    inflater.inflate(R.layout.fragment_messages, container, false);

        List messageList    =   MessageManager.getInstance().getMessages();
        messageAdapter      =   new MessageListAdapter(messageList, getActivity());
        ListView listView   =   (ListView) view.findViewById(R.id.messageList);
        listView.setAdapter(messageAdapter);
        listView.setOnItemClickListener(this);

        return view;
    }

    private void showMessageInspectFragment(Message message)
    {
        Fragment msgInspectFragment     =   new MessageInspectFragment();
        Bundle data                     =   new Bundle();
        data.putSerializable("message", message);
        msgInspectFragment.setArguments(data);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, msgInspectFragment)
                .addToBackStack(null)
                .commit();
    }

    public void loadMessages()
    {
        List messageList    =   MessageManager.getInstance().getMessages();
        messageAdapter.setList(messageList);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Message message =   messageAdapter.getMessageAt(position);
        if(message != null)
        {
            message.setRead(true);
            showMessageInspectFragment(message);
        }
    }
}
