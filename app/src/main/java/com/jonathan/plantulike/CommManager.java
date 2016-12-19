package com.jonathan.plantulike;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import static android.R.attr.data;

/**
 * Created by jonathan on 19/12/16.
 */

public class CommManager  extends AsyncTask<String, Void, String> {
    private final String USER_AGENT = "Mozilla/5.0";
    private ProgressDialog dialog = null;

    @Override
    protected String doInBackground(String[] params) {
        try {
            if(params[0].equals("POST")) {
                Log.d("Comms","IN POST");
                sendPost(params[1], params[2]);
            }
            else if (params[0].equals("GET"))
            {
                Log.d("Comms","IN GET");

                sendGet(params[1]);
            } else {
                return "Need to specify GET or POST";
            }
        } catch (Exception e) {
            Log.e("Comms", "Error sending message:" + e.getMessage());
        }
        return "Success!";
    }

    public void sendGet(String urlString) {
        try {
            URL url = new URL(urlString);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            Log.d("Comms","\nSending 'GET' request to URL : " + url);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            Log.d("Response", response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // HTTP POST request
    private void sendPost(String urlString, String sourceFileUri) throws Exception {

        String fileName = sourceFileUri;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        File sourceFile = new File(sourceFileUri);

        File f = new File(sourceFileUri);
        Uri contentUri = Uri.fromFile(f);

        //oldcode
//            URL url = new URL(urlString);
//            HttpURLConnection con = (HttpURLConnection) url.openConnection();
//
//            //add request header
//            con.setRequestMethod("POST");
//            con.setRequestProperty("User-Agent", USER_AGENT);
//            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
//
//            String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
//            // Send post request
//            con.setDoOutput(true);
//            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//            wr.writeBytes(urlParameters);
//            wr.flush();
//            wr.close();
//
//            int responseCode = con.getResponseCode();
//            System.out.println("\nSending 'POST' request to URL : " + url);
//            System.out.println("Post parameters : " + urlParameters);
//            System.out.println("Response Code : " + responseCode);
//
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(con.getInputStream()));
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            //print result
//            System.out.println(response.toString());
        if (!sourceFile.isFile()) {
            dialog.dismiss();
            Log.e("uploadFile", "Source File not exist ");
            return;
        }
        else try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);

            // Open a HTTP  connection to  the URL
            con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true); // Allow Inputs
            con.setDoOutput(true); // Allow Outputs
            con.setUseCaches(false); // Don't use a Cached Copy
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("ENCTYPE", "multipart/form-data");
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            con.setRequestProperty("uploaded_file", fileName);
            dos = new DataOutputStream(con.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"filedata\";filename=\""
                    + fileName + "\"" + lineEnd);

            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = con.getResponseCode();
            String serverResponseMessage = con.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Upload", "Exception : " + e.getMessage(), e);
        }
    }

}

