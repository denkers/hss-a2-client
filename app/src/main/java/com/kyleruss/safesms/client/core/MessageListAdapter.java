//======================================
//  Kyle Russell
//  AUT University 2016
//  Highly Secured Systems A2
//======================================

package com.kyleruss.safesms.client.core;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyleruss.safesms.client.R;

import java.util.Collection;
import java.util.List;

public class MessageListAdapter extends ArrayAdapter<Message>
{
    private final Activity context;
    private List<Message> messages;

    public MessageListAdapter(List<Message> messages, Activity context)
    {
        super(context, R.layout.user_list_row, messages);
        this.messages       =   messages;
        this.context        =   context;
    }

    //Clears and updates the messages list with the passed  message list
    public void setList(Collection<Message> messageList)
    {
        super.clear();
        super.addAll(messageList);
        super.notifyDataSetChanged();
        messages    =   (List<Message>) messageList;
    }

    public List<Message> getMessageList()
    {
        return messages;
    }

    public Message getMessageAt(int pos)
    {
        return messages.get(pos);
    }

    //Returns a view that displays the message sender and received date
    //Also shows a drawable which is changed depenending on if the message has been read or not
    public View getView(int position, View view, ViewGroup parent)
    {
        LayoutInflater inflater     =   context.getLayoutInflater();
        View viewRow                =   inflater.inflate(R.layout.message_list_row, null, true);
        Message message             =   messages.get(position);
        ImageView statusImageView   =   (ImageView) viewRow.findViewById(R.id.messageImageview);
        TextView messageInfoView    =   (TextView) viewRow.findViewById(R.id.messageInfoView);
        String recvDate             =   message.recvDateToString();

        messageInfoView.setText("From: " + message.getFrom() + "\nReceived at: " + recvDate);
        statusImageView.setImageResource(message.isRead()? R.drawable.notification_read : R.drawable.notification_unread);
        return viewRow;
    }
}
