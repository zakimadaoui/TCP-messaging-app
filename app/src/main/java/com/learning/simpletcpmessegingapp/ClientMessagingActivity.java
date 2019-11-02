package com.learning.simpletcpmessegingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientMessagingActivity extends AppCompatActivity {

    private static final int DEFAULT_PORT = 1027 ;
    private List<String> allMessages = new ArrayList<>();

    private Socket connection;
    private BufferedReader incomingData;
    private InputStream in;
    private PrintWriter out;
    private MessageAdapter adapter ;

    private String inMessage ;
    private String outMessage ;

    public static final String HANDSHAKE = "SALAM ALIKOM" ;
    private String proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        Intent intent = getIntent() ;
        proxy = intent.getStringExtra("proxy");
        Log.d("TAG", "onCreate: Proxy: " + proxy);


        final EditText sendEditText = findViewById(R.id.send_editText);
        final Button sendButton = findViewById(R.id.send_button);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message =  sendEditText.getText().toString() ;

                if (message.trim().isEmpty()){

                }

                else {

                    outMessage = "out:" + message;
                    try {
                        out = new PrintWriter(connection.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    SendThread sendThread = new SendThread(message);
                    sendThread.start();

                    allMessages.add(outMessage);
                    adapter.notifyItemInserted(allMessages.size() - 1);
                }

                sendEditText.setText(""); // clear the edit text


            }
        });


        //setting the recycler view
        RecyclerView conversationRecycler = findViewById(R.id.conversation_recycler);
        adapter = new MessageAdapter();
        adapter.setAllMessages(allMessages);
        conversationRecycler.setLayoutManager(new LinearLayoutManager(this));
        conversationRecycler.setAdapter(adapter);
        conversationRecycler.setItemViewCacheSize(100);


        MessagingAsync messagingAsync = new MessagingAsync();
        messagingAsync.execute();



    }

    private class MessagingAsync extends AsyncTask<Void,String,Void> {

        ProgressDialog dialog ;
        String TAG = "MessagingAsync";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ClientMessagingActivity.this);
            dialog.setMessage("Connecting ......");
            dialog.show();


        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Log.d(TAG, "doInBackground: Connecting to" +proxy +  "on port"  + DEFAULT_PORT);
                connection = new Socket(proxy,DEFAULT_PORT); //sending a request to the server
                incomingData = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                out = new PrintWriter(connection.getOutputStream());

                String inHandShake = incomingData.readLine();
                if (!inHandShake.equals(HANDSHAKE)){
                    throw  new IOException("different handshake, thus this isn't the required server");
                }

                out = new PrintWriter(connection.getOutputStream());
                out.println(HANDSHAKE); // send handshake
                out.flush();

                ReceiveListener receiveListener  = new ReceiveListener();
                receiveListener.execute();


            } catch (IOException e) {

                publishProgress("connection failed");

                Log.d("MessagingAsync",
                        "doInBackground: An error occurred while opening connection." + e.getMessage());
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (values[0].equals("connection failed")){

                dialog.dismiss();

                Toast.makeText(ClientMessagingActivity.this,
                        "An error occurred while opening connection.", Toast.LENGTH_SHORT).show();
            }

            else dialog.setMessage(values[0]);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }





    //============================== receiving messages in this async ==============================

    private class ReceiveListener extends AsyncTask<Void,String,Void>{


        @Override
        protected Void doInBackground(Void... voids) {

            while (true){

                try {

                    String tempMessage = incomingData.readLine();
                    if (tempMessage != null){
                        inMessage = "in:" +tempMessage;
                        publishProgress(inMessage);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            allMessages.add(values[0]);
            adapter.notifyItemInserted(allMessages.size()-1);

        }
    }


    private class SendThread extends Thread {

        String message ;

        SendThread(String message){
            this.message = message ;
        }

        @Override
        public void run() {
            super.run();
            out.println(message);
            out.flush();
        }
    }

}
