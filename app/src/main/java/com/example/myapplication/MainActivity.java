package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    final int ACTIVITY_CHOOSE_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendMessage(View view)
    {
        System.out.println("Hey");
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType(("application/vnd.android.package-archive"));
        Intent intent = Intent.createChooser(chooseFile, "Choose an APK");
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
        {
            System.out.println("Result is ok: " + requestCode);
            if (requestCode == ACTIVITY_CHOOSE_FILE)
            {
                System.out.println("Before URI");
                final Uri uri = data.getData();

                HttpClient client = new HttpClient();
                Object args[] = {uri, this};

                String risk_score = null;
                try {
                    risk_score = (String)client.execute(args).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), risk_score, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
