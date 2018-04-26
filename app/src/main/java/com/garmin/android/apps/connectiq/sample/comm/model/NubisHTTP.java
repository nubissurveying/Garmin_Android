package com.garmin.android.apps.connectiq.sample.comm.model;

import android.content.Context;

/**
 * Created by cal on 4/24/18.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.InflaterInputStream;


public class NubisHTTP extends AsyncTask<String, Integer, String> {

    public static final int H_UPLOAD = 0;
    public static final int H_DOWNLOAD = 1;
    public static final int H_CHECK_SERVER = 2;

    private boolean isDemo = true;

    public NubisAsyncResponse delegate=null;

    private Context context;
    private NubisDelayedAnswer delayedAnswer;
    int serverResponseCode;
    String serverResponseMessage;
    int deleteId = -1;
    public String serverInstructions;
    int communicationType = 0;
    private String serverURL = isDemo? "http://10.120.72.9:8888/ema/index.php" : "https://uas.usc.edu/survey/uas/ema/daily/index.php";

    public NubisHTTP(Context context, NubisDelayedAnswer delayedAnswer, NubisAsyncResponse delegate, int deleteId, int communicationType){
        this.context = context;
        this.delayedAnswer = delayedAnswer;
        this.delegate = delegate;
        this.deleteId = deleteId;
        this.communicationType = communicationType;

    }

    @Override
    protected String doInBackground(String... params) {
        try {
            publishProgress((int) 20);
            if (delayedAnswer.getType() == NubisDelayedAnswer.N_GET){

                String upLoadServerUri = serverURL + "?ema=1&q=" + Encrypt(delayedAnswer.getGetString());
                Log.d("emcryp string", Encrypt(delayedAnswer.getGetString()));
                URL url = new URL(upLoadServerUri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();  // Open a HTTP  connection to  the URL
                serverResponseCode = conn.getResponseCode();
                serverResponseMessage = conn.getResponseMessage();
                //((NubisApplication)context.getApplicationContext()).settings.readSettingsFromString(serverResponseMessage);
                return serverResponseMessage;
            }
            else if (delayedAnswer.getType() == NubisDelayedAnswer.N_GET_READ || delayedAnswer.getType() == NubisDelayedAnswer.N_CHECK_SERVER){
                String upLoadServerUri = serverURL + "?ema=1&q=" + Encrypt(delayedAnswer.getGetString());
                URL url = new URL(upLoadServerUri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();  // Open a HTTP  connection to  the URL
                conn.setConnectTimeout(5000);  //need timeout here!
                serverResponseCode = conn.getResponseCode();
                serverResponseMessage = conn.getResponseMessage();
                if(!String.valueOf(serverResponseCode).startsWith("2")){
                    //  error!!
                    return serverResponseMessage;
                }
                else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String str;
                    String response = "";
                    while ((str = in.readLine()) != null) {
                        response += (str +"\n");
                        publishProgress((int) 20);
                    }
                    in.close();

                    response = Uncompress(response); //uncompress
                    serverInstructions = Unencrypt(response).trim();  //unencrypt
                    return serverInstructions;
                }


            }

            else if (delayedAnswer.getType() == NubisDelayedAnswer.N_POST){
                String upLoadServerUri = serverURL + "?ema=1&q=" + Encrypt(delayedAnswer.getGetString());
                Log.d("emcryp string", Encrypt(delayedAnswer.getGetString()));
                URL url = new URL(upLoadServerUri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                byte[] outputInBytes = delayedAnswer.getPostData().getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();

                serverResponseCode = conn.getResponseCode();
                serverResponseMessage = conn.getResponseMessage();


                os.write( outputInBytes );
                os.close();
                //((NubisApplication)context.getApplicationContext()).settings.readSettingsFromString(serverResponseMessage);
                return serverResponseMessage;

            }
            else if (delayedAnswer.getType() == NubisDelayedAnswer.N_POST_FILE){
//				 String serverURL = "http://10.120.67.5:8888/ema/index.php";
                String upLoadServerUri = serverURL + "?ema=1&q=" + Encrypt(delayedAnswer.getGetString());
//				  String upLoadServerUri = serverURL + "?ema=1&q=" + Encrypt(delayedAnswer.getGetString());
                Log.d("emcryp string", Encrypt(delayedAnswer.getGetString()));
                Log.d("uploadServerUri", upLoadServerUri);
                URL url = new URL(upLoadServerUri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + NubisDelayedAnswer.N_boundary);
                conn.setRequestProperty("uploaded_file", "test");

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(NubisDelayedAnswer.N_twoHyphens + NubisDelayedAnswer.N_boundary + NubisDelayedAnswer.N_lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + "test" + "\"" + NubisDelayedAnswer.N_lineEnd);

                dos.writeBytes(NubisDelayedAnswer.N_lineEnd);
//				Log.e("bakgrand upload", dos.toString());
//				Log.e("bakgrand upload", delayedAnswer.toString());
                delayedAnswer.getByteArrayOutputStream().writeTo(dos);

                // send multipart form data necesssary after file data...
                dos.writeBytes(NubisDelayedAnswer.N_lineEnd);
                dos.writeBytes(NubisDelayedAnswer.N_twoHyphens + NubisDelayedAnswer.N_boundary + NubisDelayedAnswer.N_twoHyphens + NubisDelayedAnswer.N_lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                serverResponseMessage = conn.getResponseMessage();

                dos.flush();
                dos.close();

                //((NubisApplication)context.getApplicationContext()).settings.readSettingsFromString(serverResponseMessage);
                return serverResponseMessage;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (communicationType == H_DOWNLOAD){
            Toast.makeText(context, "downloading...", Toast.LENGTH_SHORT).show();
        }
        else if (communicationType == H_CHECK_SERVER){
            Toast.makeText(context, "connecting...", Toast.LENGTH_SHORT).show();
        }
        else {
            String responseStr = "";
            if (deleteId > 0){
                responseStr = " item " + deleteId;
            }
            Toast.makeText(context, "uploading..." + responseStr, Toast.LENGTH_SHORT).show();
        }

        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        // PowerManager pm = (PowerManager) tempcontent.getSystemService(Context.POWER_SERVICE);
        //       mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
        //            getClass().getName());
        //       mWakeLock.acquire();
        //  mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        if (communicationType == H_DOWNLOAD){
            Toast.makeText(context, "downloading...", Toast.LENGTH_SHORT).show();
        }
        else if (communicationType == H_CHECK_SERVER){
            Toast.makeText(context, "connecting...", Toast.LENGTH_SHORT).show();
        }
        else {
            String responseStr = "";
            if (deleteId > 0){
                responseStr = " item " + deleteId;
            }
            Toast.makeText(context, "uploading..." + responseStr, Toast.LENGTH_SHORT).show();
        }





        //  Toast.makeText(context, "downloading..." + deleteId, Toast.LENGTH_SHORT).show();
        // if we get here, length is known, now set indeterminate to false
        //mProgressDialog.setIndeterminate(false);
        //mProgressDialog.setMax(100);
        //mProgressDialog.setProgress(progress[0]);

    }

    @Override
    protected void onPostExecute(String result) {
        //mWakeLock.release();
        //mProgressDialog.dismiss();
        // Toast.makeText(context,"downloading....." + deleteId, Toast.LENGTH_SHORT).show();
        if (delegate != null){
            delegate.processFinish(result, serverResponseCode, serverResponseMessage, delayedAnswer, deleteId);
        }
    }


    public static String Unencrypt(String inputStr){
        try {
            MCrypt mcrypt = new MCrypt();
            return new String( mcrypt.decrypt( inputStr) );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputStr;
    }

    public static String Encrypt(String inputStr){
        try {
            MCrypt mcrypt = new MCrypt();
            return MCrypt.bytesToHex( mcrypt.encrypt(inputStr) );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return inputStr;
    }


    public static String Uncompress(String inputStr){
        String unzipped = "";
        try {
            byte[] zbytes = Base64.decode(inputStr, Base64.DEFAULT);
            // byte[] zbytes = zippedText.getBytes("ISO-8859-1");
            // Add extra byte to array when Inflater is set to true
            byte[] input = new byte[zbytes.length + 1];
            System.arraycopy(zbytes, 0, input, 0, zbytes.length);
            input[zbytes.length] = 0;
            ByteArrayInputStream bin = new ByteArrayInputStream(input);
            InflaterInputStream in = new InflaterInputStream(bin);
            ByteArrayOutputStream bout = new ByteArrayOutputStream(512);
            int b;
            while ((b = in.read()) != -1) {
                bout.write(b); }
            bout.close();
            unzipped = bout.toString();
        }
        catch (IOException io) {

        }
        catch (Exception e){

        }
        return unzipped;

    }

}
