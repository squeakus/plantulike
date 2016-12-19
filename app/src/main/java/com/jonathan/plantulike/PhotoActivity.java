package com.jonathan.plantulike;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class PhotoActivity extends AppCompatActivity {
    private ImageView mImageView;
    private TextView res1;
    private TextView res2;
    private TextView res3;
    private TextView res4;
    private TextView res5;
    private TextView prob1;
    private TextView prob2;
    private TextView prob3;
    private TextView prob4;
    private TextView prob5;
    private TextView timetaken;
    private String photofile;
    private static String serverURL = "http://192.168.1.14:5000/classify_upload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        mImageView = (ImageView) findViewById(R.id.imageView1);
        res1 = (TextView) findViewById(R.id.result1);
        res2 = (TextView) findViewById(R.id.result2);
        res3 = (TextView) findViewById(R.id.result3);
        res4 = (TextView) findViewById(R.id.result4);
        res5 = (TextView) findViewById(R.id.result5);
        prob1 = (TextView) findViewById(R.id.prob1);
        prob2 = (TextView) findViewById(R.id.prob2);
        prob3 = (TextView) findViewById(R.id.prob3);
        prob4 = (TextView) findViewById(R.id.prob4);
        prob5 = (TextView) findViewById(R.id.prob5);
        timetaken = (TextView) findViewById(R.id.timetaken);


        Intent intent = getIntent();
        photofile = intent.getStringExtra(MainActivity.EXTRA_FILE);
        setPic();
        new ClassifyImage().execute(serverURL, photofile);
    }


    private void setPic() {
        // Get the dimensions of the View
        int targetW = 640;
        int targetH = 480;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photofile, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(photofile, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }



    private class ClassifyImage extends AsyncTask<String, Void, String> {

        private ProgressDialog mProgressDialog;
        private final String USER_AGENT = "Mozilla/5.0";

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(PhotoActivity.this);
            mProgressDialog.setMessage("Uploading Image");
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String[] params) {
            String results = "";
            try {
                results = sendPost(params[0], params[1]);

            } catch (Exception e) {
                Log.e("Comms", "Error sending message:" + e.getMessage());
            }
            Log.d("comms","End");

            return results;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(getBaseContext(), "Successfully classified image!", Toast.LENGTH_LONG).show();
                processResults(result);
            } else {
                Toast.makeText(getBaseContext(), "Error, could not classify image.", Toast.LENGTH_LONG).show();
                finish();
            }

        }

        private void processResults(String resultString) {
            try {
                JSONObject mainObject = new JSONObject(resultString);
                JSONArray result = mainObject.getJSONArray("result");
                boolean success = mainObject.getBoolean("success");
                String time = mainObject.getString("time");
                timetaken.setText(time);

                JSONArray classify = result.getJSONArray(0);
                res1.setText(classify.get(0).toString());
                prob1.setText(classify.get(1).toString());
                classify = result.getJSONArray(1);
                res2.setText((CharSequence) classify.get(0));
                prob2.setText((CharSequence) classify.get(1));

                classify = result.getJSONArray(2);
                res3.setText((CharSequence) classify.get(0));
                prob3.setText((CharSequence) classify.get(1));

                classify = result.getJSONArray(3);
                res4.setText((CharSequence) classify.get(0));
                prob4.setText((CharSequence) classify.get(1));

                classify = result.getJSONArray(4);
                res5.setText((CharSequence) classify.get(0));
                prob5.setText((CharSequence) classify.get(1));



                for (int i=0; i < result.length(); i++) {
                    classify = result.getJSONArray(i);
                    String classstr = "Result "+ classify.get(0) + " Probability:" + classify.get(1);
                    Log.d("Result:", classstr);
                }
            } catch (JSONException e){
                e.printStackTrace();
                Log.e("JSON", "JSON exception : " + e.getMessage());

            }

        }
        // HTTP POST request
        private String sendPost(String urlString, String sourceFileUri) throws Exception {

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
            StringBuffer response = new StringBuffer();


            if (!sourceFile.isFile()) {
                Log.e("uploadFile", "Source File not exist ");
                return "";
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

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Upload", "Exception : " + e.getMessage(), e);
            }
            return response.toString();
        }

    }


    public void selfDestruct(View view) {
        finish();
    }

}
