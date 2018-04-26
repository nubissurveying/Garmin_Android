package com.garmin.android.apps.connectiq.sample.comm.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by cal on 4/24/18.
 */

public class AcceFileManager {
    double standardGravity = 0;
    private Context context;
    private String filename = "";
    private String initString = "";
    static String HTTPReturnString = "";
    StringBuilder appendBuffer ;
    private int appendCount;
    private int uploadCount;
    private int uploadInterval = 60 ;
    private int appendInterval = 60 /2;
    private boolean hasInternet = true;

    public AcceFileManager(Context context){
        this.context = context;
        filename = context.getFilesDir().getPath() + "/acce_data_garmin.txt";
        appendBuffer = new StringBuilder();
        initFile(context);
        appendCount = 0;
        uploadCount = 0;
    }
    public void processDataFromDevice(String message){
        Log.d("ProcessDataFromDevice", message);
        String[] messages = message.split(" +");
        double svm = 0;
        if(messages.length >= 4) svm = calculateSVM(messages[1], messages[2], messages[3]);


        Log.d("ProcessDataFromDevice", "current svm" + svm);


        uploadPrepare(DateUtil.stringifyAll(Calendar.getInstance()) + " " + svm + "\n");

//        Log.d("upload file", loadFile(context));
    }

    public void uploadPrepare(String svmPerSec){
        appendBuffer.append(svmPerSec);
        appendCount++;
        uploadCount++;
        if(appendCount >= appendInterval){
            Log.d("AccServece","time to append");
            appendFile(context,appendBuffer.toString());
            appendBuffer = new StringBuilder();
            appendCount = 0;
        }else if(uploadCount >= uploadInterval && hasInternet){
            Log.d("AccServece","time to upload");
            uplaodFile(context);
            resetFile(context);
            uploadCount = 0;
        }

    }

    public double calculateSVM(String a, String b, String c){
        double x = Double.parseDouble(a);
        double y = Double.parseDouble(b);
        double z = Double.parseDouble(c);
        double res = Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2)) - standardGravity;
        return res / 100;

    }



    public  void  initFile(Context context){

        filename = context.getFilesDir().getPath() + "/acce_data_garmin.txt";
//        if(checkExist(context,rtid)) return ;
        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(filename);
            outputStream.write(initString.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("on create", "acc file created" + filename);


    }
    public void appendFile(Context context, String str){

        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(filename,true);
            outputStream.write(str.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.d("on create", "acc file append");
    }
    public boolean checkExist(Context context){
        File ex = new File(filename);
        return ex.exists();
    }
    public  String loadFile(Context context){

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String str = "";
        String line;
        try{
            while((line=bufferedReader.readLine()) != null){
                str += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            fileInputStream.getChannel().position(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("load file content", str);
        return str;
    }
    public void uplaodFile(Context context){


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.d("AccFileManager", "upload File");
        try {
            NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_POST_FILE);
            delayedanswer.addGetParameter("version", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
            delayedanswer.addGetParameter("phonets", formatter.format(Calendar.getInstance().getTimeInMillis()));
            delayedanswer.addGetParameter("p", "uploadacceldata");
            delayedanswer.addGetParameter("ema", "1");
            delayedanswer.addFileName(filename);
            delayedanswer.setByteArrayOutputStream();
            upLoad(context,delayedanswer, true, -1, NubisHTTP.H_UPLOAD);


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void upLoad(Context context, NubisDelayedAnswer delayedAnswer, boolean wait, int deleteId, int communicationType) {
        //Context context, NubisDelayedAnswer delayedAnswer, NubisAsyncResponse delegate
        try {
            NubisHTTP httpCom = new NubisHTTP(context, delayedAnswer, null, deleteId, communicationType);
            if (wait) {
                httpCom.serverInstructions = "";
                httpCom.execute(); //doInBackground();//.get(210000, TimeUnit.MILLISECONDS);

                HTTPReturnString = httpCom.serverInstructions;

            } else {
                httpCom.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetFile(Context context){
        if(!checkExist(context)) initFile(context);
        try {
            FileOutputStream overWrite = new FileOutputStream(filename,false);
            overWrite.write(("").getBytes());
            overWrite.flush();
            overWrite.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    BroadcastReceiver networkAvailableReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            hasInternet = (cm.getActiveNetworkInfo() != null);
        }
    };
}
