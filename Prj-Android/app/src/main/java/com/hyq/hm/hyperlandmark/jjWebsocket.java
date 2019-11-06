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
    public boolean Detected;
    public boolean Predict;
    public boolean Fail;
    public String result;

    @Override
    protected Long doInBackground(String... strings) {
        return null;
    }

    public void init() {
        try {
            IO.Options options = new IO.Options();
            options.transports = new String[]{WebSocket.NAME};
            mSocket = IO.socket("http://15.164.96.191:5000", options);
        } catch (URISyntaxException e) {
            Log.e("err", e.toString());
            System.out.println("Can't Connect");
        }
        Detected = false;
        Predict = false;
        this.connect();
    }

    private void connect() {
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on("greet", onReceivedgreetMessage);
        mSocket.on("Detecting result",onDetectingResultMessage);
        mSocket.on("Predict result",onPredictResultMessage);
        mSocket.on(Socket.EVENT_ERROR, onError);
        mSocket.connect();
    }

    public boolean connected() {
        return mSocket.connected();
    }

    public void send(String Data, int type) {
        System.out.println(Data);
        switch (type) {
            case 0 :
                mSocket.emit("Ready", "Ready to take a data...");
                break;
            case 1 :
                mSocket.emit("PreviewData", Data);
                break;
            case 2 :
                mSocket.emit("AlbumData", Data);
                break;
        }
    }

    public void disconnect() {
        mSocket.disconnect();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String s = "HI Server!!!";
            mSocket.emit("greet", s);
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Exception e = (Exception) args[0];
            Log.e("error", "Transport error " + e);
            Log.e("error", "ConnectError Occured!!");
        }
    };

    private Emitter.Listener onReceivedgreetMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("onGreetMessage");
            String s = args[0].toString();
            System.out.println(s);
        }
    };

    private Emitter.Listener onDetectingResultMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("onDetectingResultMessage");
            String s = args[0].toString();
//            System.out.println(s.charAt(2));
            if(s.charAt(2) == '1'){
                mSocket.emit("Detected","Success");
                Detected = true;
            }
            else{
                mSocket.emit("Detected","Fail");
                Fail = true;
            }
        }
    };

    private Emitter.Listener onPredictResultMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("onPredictResultMessage");
            String s = args[0].toString();
            System.out.println(s);
            for(int i=0; i<s.length(); i++){
                if(s.charAt(i) == 'B'){
                    result = s.substring(i+16,s.length()-2);
                    break;
                }
            }
            Predict = true;
            System.out.println(result);
        }
    };

    private Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("error", "Error Occured!!");
            Log.e("error", args[0].toString());
        }
    };
}
