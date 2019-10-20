package com.hyq.hm.hyperlandmark;

import android.os.AsyncTask;
import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;


        public class jjWebsocket extends AsyncTask<String, String, Long> {
            private Socket mSocket;

            @Override
            protected Long doInBackground(String... strings) {
                return null;
            }

            public void init(){
        try{
            IO.Options options = new IO.Options();
            options.transports = new String[] {WebSocket.NAME};
            mSocket = IO.socket("http://54.180.122.119:5000",options);
        }
        catch (URISyntaxException e){
            Log.e("err",e.toString());
            System.out.println("Can't Connect");
        }
        this.connect();
    }

    private void connect(){
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR,onConnectError);
        mSocket.on("greet",onReceivedMessage);
        mSocket.on(Socket.EVENT_ERROR,onError);
        mSocket.connect();
    }

    public boolean connected(){
        return mSocket.connected();
    }

    public void send(String Data){
        System.out.println(Data);
        switch (Data){
            case "Ready":
                mSocket.emit("Ready","Ready to take a data...");
                break;
            default:
                mSocket.emit("PreviewData",Data);
        }
    }

    public void disconnect(){
        mSocket.disconnect();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String s = "HI Server!!!";
            mSocket.emit("greet",s);
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Exception e = (Exception) args[0];
            Log.e("error", "Transport error " + e);
            Log.e("error","ConnectError Occured!!");
        }
    };

    private Emitter.Listener onReceivedMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("onReceivedMessage");
            String s = args[0].toString();
            System.out.println(s);
        }
    };

    private Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("error","Error Occured!!");
            Log.e("error",args[0].toString());
        }
    };
}
