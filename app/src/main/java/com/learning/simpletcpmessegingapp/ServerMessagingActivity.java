package com.learning.simpletcpmessegingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
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

public class ServerMessagingActivity extends AppCompatActivity {

    private static final int DEFAULT_PORT = 1027 ;
    private List<String> allMessages = new ArrayList<>();

    ServerSocket listener ;
    Socket connection;
    BufferedReader incomingData;
    InputStream in;
    PrintWriter out;
    MessageAdapter adapter ;

    String inMessage ;
    String outMessage ;

    public static final String HANDSHAKE = "SALAM ALIKOM" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

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
                    SendThread th = new SendThread(message);
                    th.start();
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



    private class MessagingAsync extends AsyncTask<Void,String,Void>{

        ProgressDialog dialog ;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ServerMessagingActivity.this);
            dialog.setMessage("Connecting ......");
            dialog.show();

        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                listener = new ServerSocket(DEFAULT_PORT);
                Log.d("MessagingAsync", "doInBackground: listening on port: " + listener.getLocalPort());
                connection = listener.accept(); // listening for request from client
                publishProgress("Connected");
                listener.close(); // closing the listener for not accepting any other request
                Log.d("MessagingAsync", "doInBackground: connection established");
                in = connection.getInputStream();

                incomingData = new BufferedReader(new InputStreamReader(in));
                out = new PrintWriter(connection.getOutputStream());
                out.println(HANDSHAKE); // send handshake
                out.flush(); // make sure that data is sent



                String inHandshake = incomingData.readLine();  // Receive handshake from client.

                Log.d("TAG", "doInBackground: inhand "+ inHandshake);

                if (inHandshake.compareTo(HANDSHAKE) < 0){
                    throw new IOException("different handshake, thus not the required client");
                }


                ReceiveListener receiveListener = new ReceiveListener();
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

                Toast.makeText(ServerMessagingActivity.this,
                        "An error occurred while opening connection.", Toast.LENGTH_SHORT).show();
            }

            else dialog.dismiss();//dialog.setMessage(values[0]);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dialog.isShowing()) dialog.dismiss();
        }
    }





    //============================== receiving messages in this async ==============================

   private class ReceiveListener extends AsyncTask<Void,String,Void>{


       @Override
       protected Void doInBackground(Void... voids) {

           while (true){
               Log.d("TAG", "doInBackground: receiving ");

               try {

                   String tempMessage = incomingData.readLine();


                   if (tempMessage != null){
                       inMessage = "in:"+tempMessage;
                       publishProgress(inMessage);
                       Log.d("TAG", "doInBackground: "+inMessage);
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



    class SendThread extends Thread {
        private String message;
        SendThread(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            Log.d("TAG", "run: gooo");
            out.println(message);
            out.flush();
            Log.d("TAG", "run: sennnnt");

        }}


}
