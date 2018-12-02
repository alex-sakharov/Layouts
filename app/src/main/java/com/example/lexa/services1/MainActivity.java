package com.example.lexa.services1;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
    private Messenger mServiceMessenger;
    private static final String TAG = "MainActivity";

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mServiceMessenger = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        bindService(RandomGeneratorService.newIntent(this), mServiceConnection, Context.BIND_IMPORTANT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        unbindService(mServiceConnection);
    }

    public void onStartServiceClick(View view) {
        RandomGeneratorService.start(this, "TEST SERVICE");
        bindService(RandomGeneratorService.newIntent(this), mServiceConnection, Context.BIND_IMPORTANT);
    }

    public void onStartActivityClick(View view) {
        startActivity(new Intent(this, ReceiverActivity.class));
    }

    public void onStopServiceClick(View view) {
        Message msg = Message.obtain (null, RandomGeneratorService.MSG_STOP);

        try {
            if (mServiceMessenger != null) {
                mServiceMessenger.send(msg);
            }
        }
        catch (RemoteException e) {
            Log.d(TAG, e.toString());
        }
    }
}
