package com.jonathan.plantulike;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
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

        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

//        String fileName = sourceFileUri;
//        File f = new File(sourceFileUri);
//        Uri contentUri = Uri.fromFile(f);
//
//        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//        conn.setRequestProperty("uploaded_file", fileName);

























        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

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

    }

}

