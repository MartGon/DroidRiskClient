package com.example.myapplication;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
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
        JSONObject result = (JSONObject) o;

        String prob = "";
        String package_name = "";
        Bitmap bmp = null;
        try {

            // Get malware prob
            JSONArray prob_array = result.getJSONArray("prob");
            double malware_prob = prob_array.getDouble(1) * 100;
            DecimalFormat df2 = new DecimalFormat("##.#");
            prob = df2.format(malware_prob);

            // Get package name
            package_name = result.getString("package_name");

            // Get icon
            String icon_str = result.getString("icon");
            icon_str = icon_str.subSequence(2, icon_str.length() - 1).toString();
            byte[] decoded = Base64.decode(icon_str, Base64.DEFAULT);
            bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Toast.makeText(mainActivity.getApplicationContext(), prob, Toast.LENGTH_SHORT).show();

        mainActivity.risk_score_display.setText(prob + '%');
        mainActivity.pkg_name_display.setText(package_name);

        mainActivity.result_layout.setVisibility(View.VISIBLE);

        mainActivity.upload_button.setEnabled(true);

        AnimationDrawable anim = (AnimationDrawable) mainActivity.apk_icon.getDrawable();
        anim.stop();
        mainActivity.apk_icon.setVisibility(View.VISIBLE);

        if(bmp == null)
            mainActivity.apk_icon.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.android));
        else
            mainActivity.apk_icon.setImageBitmap(bmp);

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

    protected JSONObject UploadAPK(Uri uri, MainActivity act)
    {
        JSONObject ret = null;

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
                ret = response;

                System.out.println(ret.toString());
            }

            conn.disconnect();
            System.out.println("Disconnected");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }
}
