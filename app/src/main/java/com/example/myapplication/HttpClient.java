package com.example.myapplication;

import android.content.ContentResolver;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class HttpClient extends AsyncTask {

    MainActivity mainActivity;

    @Override
    protected void onPostExecute(Object o) {
        String result = (String)o;

        Toast.makeText(mainActivity.getApplicationContext(), result, Toast.LENGTH_SHORT).show();

        mainActivity.risk_score_display.setText(result + '%');
        mainActivity.result_layout.setVisibility(View.VISIBLE);
        mainActivity.upload_button.setEnabled(true);

        AnimationDrawable anim = (AnimationDrawable) mainActivity.apk_icon.getDrawable();
        anim.stop();
        mainActivity.apk_icon.setVisibility(View.VISIBLE);
        mainActivity.apk_icon.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.android));
    }

    @Override
    protected Object doInBackground(Object[] objects)
    {
        if(objects.length < 2)
            return null;

        Uri uri = (Uri)objects[0];
        mainActivity = (MainActivity) objects[1];

        return UploadAPK(uri, mainActivity);
    }

    protected String UploadAPK(Uri uri, MainActivity act)
    {
        String ret = null;

        final ContentResolver resolver = act.getContentResolver();
        try {
            System.out.println("Hello from other thread");
            InputStream is = resolver.openInputStream(uri);

            URL url = null;
            url = new URL("http://192.168.2.127:3000/apk");

            // Constants
            final String filename = "uploaded_apk.apk";
            final String line_end = "\r\n";
            final String boundary =  "*****";
            final String twoHyphens =  "--";

            // Open a HTTP  connection to  the URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("apk", filename);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());

            // Write Multipart data body start
            os.writeBytes(twoHyphens + boundary + line_end);
            os.writeBytes("Content-Disposition: form-data; name=\"apk\";filename=\""+ filename + "\"" + line_end);
            os.writeBytes("Content-Type: application/octet-stream" + line_end);
            os.writeBytes(line_end);

            // Write body data
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0)
                os.write(buf, 0, len);
            is.close();

            // Write Mulitpart data body ending
            os.writeBytes(line_end);
            os.writeBytes(twoHyphens + boundary + twoHyphens + line_end);
            os.flush();
            os.close();

            // Handle response
            int response_code= conn.getResponseCode();
            if(response_code == 200)
            {
                System.out.println("Reading response");
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    total.append(line).append('\n');
                }

                // Get probability of malware
                JSONObject response = new JSONObject(total.toString());
                JSONArray prob_array = response.getJSONArray("prob");
                double malware_prob = prob_array.getDouble(1) * 100;
                DecimalFormat df2 = new DecimalFormat("##.#");
                ret = df2.format(malware_prob);

                System.out.println(ret);
            }

            conn.disconnect();
            System.out.println("Disconnected");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }
}
