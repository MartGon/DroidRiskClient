package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    final int ACTIVITY_CHOOSE_FILE = 1;

    // GUI
    TextView risk_score_display;
    TextView pkg_name_display;

    LinearLayout risk_layout;
    LinearLayout result_layout;

    Button upload_button;

    ImageView apk_icon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        risk_score_display = findViewById(R.id.risk_score);
        pkg_name_display = findViewById(R.id.pkg_name_display);

        risk_layout = findViewById(R.id.risk_layout);
        result_layout = findViewById(R.id.result_layout);

        upload_button = findViewById(R.id.upload_button);

        apk_icon = findViewById(R.id.apk_icon);

        result_layout.setVisibility(View.INVISIBLE);
        apk_icon.setVisibility(View.INVISIBLE);
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
                client.execute(args);

                upload_button.setEnabled(false);

                apk_icon.setVisibility(View.VISIBLE);
                result_layout.setVisibility(View.INVISIBLE);

                apk_icon.setImageDrawable(getResources().getDrawable(R.drawable.loading));
                AnimationDrawable anim = (AnimationDrawable) apk_icon.getDrawable();
                anim.start();
            }
        }
    }
}
