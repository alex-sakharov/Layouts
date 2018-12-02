package com.example.lexa.layouts;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.Random;

public class HorizontalLinearActivity extends Activity {
    private View[] mViews = new View[3];
    private RandomDataBroadcastReceiver mRandomDataBroadcastReceiver;

    View getView(int index) {
        return mViews[index];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horizontal_linear);

        mViews[0] = findViewById(R.id.view1);
        mViews[1] = findViewById(R.id.view2);
        mViews[2] = findViewById(R.id.view3);

        mRandomDataBroadcastReceiver = new RandomDataBroadcastReceiver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mRandomDataBroadcastReceiver, RandomGeneratorService.getNewDataIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mRandomDataBroadcastReceiver);
    }

    private static class RandomDataBroadcastReceiver extends BroadcastReceiver {
        private final WeakReference<HorizontalLinearActivity> mActivity;

        RandomDataBroadcastReceiver(HorizontalLinearActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final HorizontalLinearActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }

            int color = intent.getIntExtra(RandomGeneratorService.PARAM_DATA, 0);

            int viewIndex = new Random().nextInt(3);
            activity.getView(viewIndex).setBackgroundColor(color);
        }
    }


}
