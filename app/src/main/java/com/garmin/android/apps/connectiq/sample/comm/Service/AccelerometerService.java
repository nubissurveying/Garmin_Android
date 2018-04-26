package com.garmin.android.apps.connectiq.sample.comm.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.garmin.android.apps.connectiq.sample.comm.DeviceActivity;
import com.garmin.android.apps.connectiq.sample.comm.MainActivity;
import com.garmin.android.apps.connectiq.sample.comm.R;
import com.garmin.android.apps.connectiq.sample.comm.model.AcceFileManager;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.exception.InvalidStateException;

import java.util.List;

/**
 * Created by cal on 4/24/18.
 */

public class AccelerometerService extends Service {
    private boolean hasInternet = true;
    private AcceFileManager acceFileManager;
    private ConnectIQ mConnectIQ;
    private IQDevice mDevice;
    private IQApp mMyApp;
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    BroadcastReceiver networkAvailableReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            hasInternet = (cm.getActiveNetworkInfo() != null);
        }
    };
    @Override
    public void onCreate() {



        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Garmin Accelerometer")
                .setContentText("Collecting accelerometer data")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("AccelerometerService", "Service start");
//        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
//        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        //registering Sensor
//        mySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//
//        mSensorManager.registerListener(this,
//                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
//                SensorManager.SENSOR_DELAY_UI);



        mConnectIQ = ConnectIQ.getInstance();
        mDevice = (IQDevice) intent.getParcelableExtra(DeviceActivity.MYDEVICE);
        mMyApp = (IQApp) intent.getParcelableExtra(DeviceActivity.MYAPP);
        acceFileManager = new AcceFileManager(this);
        try {

            mConnectIQ.registerForAppEvents(mDevice, mMyApp, new ConnectIQ.IQApplicationEventListener() {

                @Override
                public void onMessageReceived(IQDevice device, IQApp app, List<Object> message, ConnectIQ.IQMessageStatus status) {

                    // We know from our Comm sample widget that it will only ever send us strings, but in case
                    // we get something else, we are simply going to do a toString() on each object in the
                    // message list.
                    StringBuilder builder = new StringBuilder();

                    if (message.size() > 0) {
                        for (Object o : message) {
                            builder.append(o.toString());
                            builder.append("\r\n");
                        }
                    } else {
                        builder.append("Received an empty message from the application");
                    }

                    Log.e("AccelerometerService", builder.toString());
                    acceFileManager.processDataFromDevice(builder.toString());
                }

            });
        } catch (InvalidStateException e) {
            Toast.makeText(this, "ConnectIQ is not in a valid state", Toast.LENGTH_SHORT).show();
        }


        //then you should return sticky
        return Service.START_STICKY;
    }


    public void onMessageReceived(IQDevice iqDevice, IQApp iqApp, List<Object> message, ConnectIQ.IQMessageStatus iqMessageStatus) {
        StringBuilder builder = new StringBuilder();

        if (message.size() > 0) {
            for (Object o : message) {
                builder.append(o.toString());
                builder.append("\r\n");
            }
        } else {
            builder.append("Received an empty message from the application");
        }

        Log.e("AccelerometerService","in message received" + builder.toString());
        acceFileManager.processDataFromDevice(builder.toString());

    }
    @Override
    public void onDestroy() {
        Log.d("AccelerometerService", "onDestroy()");
        try {
            mConnectIQ.unregisterForApplicationEvents(mDevice,mMyApp);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
        stopForeground(true);//
        super.onDestroy();
    }
}
