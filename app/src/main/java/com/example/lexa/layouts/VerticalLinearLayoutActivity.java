package com.example.lexa.layouts;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class VerticalLinearLayoutActivity extends Activity {
    private static final String TAG = "VerticalLinearLayout";

    private Messenger mServiceMessenger;
    private TextView mTextView;
    private RandomDataBroadcastReceiver  mRandomDataBroadcastReceiver ;

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

    TextView getTextView() {return mTextView;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vertical_linear);

        mTextView = findViewById(R.id.text_view);

        mRandomDataBroadcastReceiver  = new RandomDataBroadcastReceiver (this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        bindService(RandomGeneratorService.newIntent(this), mServiceConnection, Context.BIND_IMPORTANT);
        registerReceiver(mRandomDataBroadcastReceiver, RandomGeneratorService.getNewDataIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        unbindService(mServiceConnection);
        unregisterReceiver(mRandomDataBroadcastReceiver);
    }

    public void onStartServiceClick(View view) {
        RandomGeneratorService.start(this, "TEST SERVICE");
        bindService(RandomGeneratorService.newIntent(this), mServiceConnection, Context.BIND_IMPORTANT);

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

    public void onStartConstraintLayoutActivityClick(View view) {
        startActivity(new Intent(this, ConstraintLayoutActivity.class));
    }

    public void onStartHorizontalLinearLayoutActivityClick(View view) {
        startActivity(new Intent(this, HorizontalLinearActivity.class));
    }

    public void onStartRelativeLayoutActivityClick(View view) {
        startActivity(new Intent(this, RelativeLayoutActivity.class));
    }

    private static class RandomDataBroadcastReceiver  extends BroadcastReceiver {
        private final WeakReference<VerticalLinearLayoutActivity> mActivity;

        RandomDataBroadcastReceiver (VerticalLinearLayoutActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final VerticalLinearLayoutActivity activity = mActivity.get();
            if (activity != null) {
                Integer color = intent.getIntExtra(RandomGeneratorService.PARAM_DATA, 0);
                activity.getTextView().setText(color.toString());
            }
        }
    }

}
