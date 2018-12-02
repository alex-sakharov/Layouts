package com.example.lexa.layouts;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.*;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class RandomGeneratorService extends JobIntentService {
    static final String PARAM_DATA = "com.example.lexa.layouts.extra.PARAM_DATA";
    static final int MSG_NEW_DATA = 1,
            MSG_STOP = 2,
            MSG_WANT_DATA = 3;

    private static final String ACTION_START = "com.example.lexa.layouts.action.START";
    private static final String ACTION_STOPPED = "com.example.lexa.layouts.action.STOPPED";
    private static final String ACTION_NEW_DATA = "com.example.lexa.layouts.action.NEW_DATA";
    private static final String PARAM_NAME = "com.example.lexa.layouts.extra.PARAM_NAME";
    private static final String TAG = "RandomGeneratorService";
    private static final int MAX_COUNT = 100;

    private final Messenger mMessenger;
    private volatile boolean mNeedStop;
    private volatile Messenger mReceiverListener;

    /**
     * Starts this service to perform action START with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void start(Context context, String name) {
        Intent intent = newIntent(context);
        intent.setAction(ACTION_START);
        intent.putExtra(PARAM_NAME, name);

        enqueueWork(context, RandomGeneratorService.class, 0, intent);
    }

    public static IntentFilter getStoppedServiceIntentFilter() {
        return new IntentFilter(ACTION_STOPPED);
    }
    public static IntentFilter getNewDataIntentFilter() {
        return new IntentFilter(ACTION_NEW_DATA);
    }

    static Intent newIntent(Context context) {
        return new Intent(context, RandomGeneratorService.class);
    }

    public RandomGeneratorService() {
        super();
        mMessenger = new Messenger(new MessageHandler(this));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        mReceiverListener = null;
        return false;
    }

    void setNeedStop() {
        this.mNeedStop = true;
    }
    void setReceiverListener(Messenger receiverListener) {
        this.mReceiverListener = receiverListener;
    }

    @Override
    protected void onHandleWork(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                final String name = intent.getStringExtra(PARAM_NAME);
                handleActionStart(name);
            }
        }
    }

    /**
     * Handle action START in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStart(String name) {
        Random random = new Random();
        int count =0;
        while (count < MAX_COUNT) {
            if  (mNeedStop) {
                Log.d(TAG, "handleActionStart: " + name + " stopped");
                sendBroadcast(new Intent (ACTION_STOPPED));
                return;
            }

            int data = random.nextInt();

            Intent dataIntent = new Intent(ACTION_NEW_DATA);
            dataIntent.putExtra(PARAM_DATA, data);
            sendBroadcast(dataIntent);

            if (mReceiverListener != null) {
                Message msg = Message.obtain(null, MSG_NEW_DATA);
                msg.obj = data;

                try {
                    mReceiverListener.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "handleActionStart: " + name + " " + count);

            count++;

            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                Log.d(TAG, e.toString());
            }
        }
        sendBroadcast(new Intent (ACTION_STOPPED));
    }

    static class MessageHandler extends Handler {
        private final WeakReference<RandomGeneratorService> mService;

        @Override
        public void handleMessage(Message msg) {
            final RandomGeneratorService service = mService.get();
            if (service == null) return;

            switch (msg.what) {
                case MSG_STOP:
                    service.setNeedStop();
                    break;
                case MSG_WANT_DATA:
                    service.setReceiverListener(msg.replyTo);
                    break;
                default:
                    throw new RuntimeException("Unknown message 'what'");
            }
        }

        MessageHandler(RandomGeneratorService service) {
            mService = new WeakReference<>(service);
        }

    }

}
