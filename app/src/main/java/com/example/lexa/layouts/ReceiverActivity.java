package com.example.lexa.layouts;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class ReceiverActivity extends Activity {

    private TextView mTextView;
    TextView getTextView() {
        return mTextView;
    }

    private Messenger mServiceMessenger, mMessenger;
    private ServiceStoppedBroadcastReceiver mServiceStoppedBroadcastReceiver;

    private static final String TAG = "ReceiverActivity";

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mServiceMessenger = new Messenger(service);

            Message msg = Message.obtain(null, RandomGeneratorService.MSG_WANT_DATA);
            msg.replyTo = mMessenger;
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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
        setContentView(R.layout.activity_receiver);

        mTextView = findViewById(R.id.textView);

        mMessenger = new Messenger( new MessageHandler(this));
        mServiceStoppedBroadcastReceiver = new ServiceStoppedBroadcastReceiver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        bindService(RandomGeneratorService.newIntent(this), mServiceConnection, Context.BIND_IMPORTANT);
        registerReceiver(mServiceStoppedBroadcastReceiver, RandomGeneratorService.getStoppedServiceIntentFilter());

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        unbindService(mServiceConnection);
        unregisterReceiver(mServiceStoppedBroadcastReceiver);
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<ReceiverActivity> mActivity;

        MessageHandler(ReceiverActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RandomGeneratorService.MSG_NEW_DATA) {
                final ReceiverActivity activity = mActivity.get();
                if (activity != null) {
                    activity.getTextView().append(msg.obj.toString());
                }
            }
        }
    }

    private static class ServiceStoppedBroadcastReceiver extends BroadcastReceiver {
        private final WeakReference<ReceiverActivity> mActivity;

        ServiceStoppedBroadcastReceiver(ReceiverActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final ReceiverActivity activity = mActivity.get();
            if (activity != null) {
                activity.getTextView().append("\nService finished\n");
            }
        }
    }

}
