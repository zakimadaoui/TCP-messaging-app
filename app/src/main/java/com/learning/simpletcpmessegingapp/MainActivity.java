package com.learning.simpletcpmessegingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText ipEditText = findViewById(R.id.ip_editText);
        final Button serverConnectBtn = findViewById(R.id.start_connection_button);
        final Button clientConnectBtn = findViewById(R.id.client_connect_button);
        RadioGroup radioGroup = findViewById(R.id.radiogroup);
        final TextView t = findViewById(R.id.textView);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {


                switch (checkedId){

                    case R.id.Server_radiobutton :

                        t.setText(getIpAddress());
                        clientConnectBtn.setVisibility(View.GONE);
                        ipEditText.setVisibility(View.GONE);
                        serverConnectBtn.setVisibility(View.VISIBLE);
                        serverConnectBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(MainActivity.this, ServerMessagingActivity.class);
                                startActivity(intent);

                            }
                        });
                        break;

                    case R.id.Client_radiobutton :
                        t.setText("");
                        clientConnectBtn.setVisibility(View.VISIBLE);
                        ipEditText.setVisibility(View.VISIBLE);
                        serverConnectBtn.setVisibility(View.GONE);


                        clientConnectBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(MainActivity.this, ClientMessagingActivity.class);
                                String proxy = ipEditText.getText().toString();

                                if (proxy.trim().isEmpty()){}
                                else {

                                    intent.putExtra("proxy",proxy);
                                    startActivity(intent);
                                }


                            }
                        });


                }



            }
        });




    }



    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

}
