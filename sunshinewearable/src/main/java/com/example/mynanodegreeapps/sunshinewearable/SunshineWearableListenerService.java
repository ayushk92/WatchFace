package com.example.mynanodegreeapps.sunshinewearable;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class SunshineWearableListenerService extends WearableListenerService {


    private static final String LOG_TAG = SunshineWearableListenerService.class.getSimpleName();

    public static final String DESCRIPTION_ICON = "desc";
    public static final String HIGH = "high";
    public static final String LOW = "low";

    private GoogleApiClient mGoogleApiClient;

    public SunshineWearableListenerService(){

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(LOG_TAG,"onDataChanged");
        for(DataEvent dataEvent : dataEvents){
            if(dataEvent.getType() == DataEvent.TYPE_CHANGED){
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();
                if(path.equals("/weather-data")){

                    int descIcon = dataMap.getInt("descIcon");
                    String high = dataMap.getString("high");
                    String low = dataMap.getString("low");

                    Intent messageIntent = new Intent();
                    messageIntent.setAction(Intent.ACTION_SEND);
                    messageIntent.putExtra(HIGH,high);
                    messageIntent.putExtra(LOW,low);
                    messageIntent.putExtra(DESCRIPTION_ICON, descIcon);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                }

            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        mGoogleApiClient = new GoogleApiClient.Builder(SunshineWearableListenerService.this)
                .addApi(Wearable.API)
                .build();
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(500, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(LOG_TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }


}