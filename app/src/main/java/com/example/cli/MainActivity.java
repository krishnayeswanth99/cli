package com.example.cli;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private int port;
    private String ip;
    private Socket socket;
    private ClientThread clientThread;
    private Thread thread;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.disp_ip);
        editText.setHint(getIP(MainActivity.this));
    }

    public void onClick(View view){
        if(view.getId() == R.id.connect_server){
            editText = findViewById(R.id.disp_ip);
            ip = editText.getText().toString();
            editText = findViewById(R.id.port);
            port = Integer.parseInt(editText.getText().toString());
            if(!checkPort(port))
                return;
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
        }
        if(view.getId() == R.id.capture){
            if(null != clientThread){
                clientThread.sendMessage("Capture");
            }
        }
        if(view.getId() == R.id.disconnect){
            if(null != clientThread){
                clientThread.sendMessage("Disconnect");
                thread.interrupt();
            }
        }
    }

    class ClientThread implements Runnable{

        private Socket socket;
        private PrintWriter out;

        @Override
        public void run(){
            try{
                socket = new Socket(ip,port);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"Connected to Server",Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (UnknownHostException e1){
                e1.printStackTrace();
            } catch (IOException e1){
                e1.printStackTrace();
            }
        }

        void sendMessage(final String message) {
            Toast.makeText(MainActivity.this,"Toasted",Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (null != socket) {
                            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                            out.println(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static InetAddress intToInetAddress(int hostAdd){
        byte[] addBytes = { (byte)(0xff & hostAdd),
                (byte)(0xff & (hostAdd >> 8)),
                (byte)(0xff & (hostAdd >> 16)),
                (byte)(0xff & (hostAdd >> 24))};
        try {
            return InetAddress.getByAddress(addBytes);
        } catch (UnknownHostException e){
            throw new AssertionError();
        }
    }

    protected boolean checkPort(int port){
        if(port < 0 || port > 0xFFFF){
            Toast.makeText(MainActivity.this,"Port number not valid!!",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public static String getIP(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().
                getSystemService(WIFI_SERVICE);
        return intToInetAddress(wifiManager.getDhcpInfo().ipAddress).getHostAddress();
    }
}
