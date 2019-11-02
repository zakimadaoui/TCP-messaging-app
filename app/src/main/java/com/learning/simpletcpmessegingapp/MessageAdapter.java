package com.learning.simpletcpmessegingapp;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {


    private List<String> allMessages = new ArrayList<>();

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_view,null);


        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int i) {


        //todo: binding data here

        String[] message = allMessages.get(i).split(":");
        TextView messageView = null;

        switch (message[0]){

            case  "in" :
                messageView = holder.receivedMessageView;
                break;
            case  "out": messageView = holder.sentMessageView;
        }


        if (messageView != null) {
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(message[1]);
        }


    }

    @Override
    public int getItemCount() {
        return allMessages.size();
    }

    public void setAllMessages(List<String> allMessages) {
        this.allMessages = allMessages;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public final TextView sentMessageView;
        public final TextView receivedMessageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

             sentMessageView = itemView.findViewById(R.id.out_message_textView);
             receivedMessageView = itemView.findViewById(R.id.in_message_textView);

        }
    }
}
