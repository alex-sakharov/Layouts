package com.example.lexa.layouts;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class ConstraintLayoutActivity extends Activity {
    private static final String TAG = "ConstraintLayout";

    private TextView mTextView;
    private Messenger mServiceMessenger, mMessenger;
    private ServiceStoppedBroadcastReceiver mServiceStoppedBroadcastReceiver;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mServiceMessenger = new Messenger(service);

            Message msg = Message.obtain(null, RandomGeneratorService.MSG_SUBSCRIBE);
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

    TextView getTextView() {
        return mTextView;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.constraint_layout);

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

        if (mServiceMessenger != null) {
            Message msg = Message.obtain(null, RandomGeneratorService.MSG_UNSUBSCRIBE);
            msg.replyTo = mMessenger;
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        unbindService(mServiceConnection);
        unregisterReceiver(mServiceStoppedBroadcastReceiver);
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<ConstraintLayoutActivity> mActivity;

        MessageHandler(ConstraintLayoutActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RandomGeneratorService.MSG_NEW_DATA) {
                final ConstraintLayoutActivity activity = mActivity.get();
                if (activity != null) {
                    activity.getTextView().append(msg.obj.toString());
                }
            }
        }
    }

    private static class ServiceStoppedBroadcastReceiver extends BroadcastReceiver {
        private final WeakReference<ConstraintLayoutActivity> mActivity;

        ServiceStoppedBroadcastReceiver(ConstraintLayoutActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final ConstraintLayoutActivity activity = mActivity.get();
            if (activity != null) {
                activity.getTextView().append("\nService finished\n");
            }
        }
    }

}
