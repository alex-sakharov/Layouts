package com.example.lexa.layouts;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.*;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Random;

public class RelativeLayoutActivity extends Activity {
    private View[] mViews = new View[3];
    private Messenger mServiceMessenger, mMessenger;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
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
            mServiceMessenger = null;
        }
    };

    View getView(int index) {
        return mViews[index];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.relative_layout);

        mViews[0] = findViewById(R.id.relative_view1);
        mViews[1] = findViewById(R.id.relative_view2);
        mViews[2] = findViewById(R.id.relative_view3);

        mMessenger = new Messenger( new MessageHandler(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(RandomGeneratorService.newIntent(this), mServiceConnection, Context.BIND_IMPORTANT);
    }

    @Override
    protected void onPause() {
        super.onPause();

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
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<RelativeLayoutActivity> mActivity;

        MessageHandler(RelativeLayoutActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RandomGeneratorService.MSG_NEW_DATA) {
                final RelativeLayoutActivity activity = mActivity.get();
                if (activity == null) {
                    return;
                }

                int color = (int) msg.obj;
                int viewIndex = new Random().nextInt(3);
                activity.getView(viewIndex).setBackgroundColor(color);
            }
        }
    }

}
