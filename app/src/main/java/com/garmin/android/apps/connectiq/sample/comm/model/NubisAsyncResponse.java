package com.garmin.android.apps.connectiq.sample.comm.model;

/**
 * Created by cal on 4/24/18.
 */

public interface NubisAsyncResponse {
    void processFinish(String output, int responseCode, String responseString, NubisDelayedAnswer delayedAnswer, int deleteId);
}
