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

import org.json.JSONException;
import org.json.JSONObject;

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

    // Logic
    JSONObject apk_reponse_msg = null;


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
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType(("application/vnd.android.package-archive"));
        Intent intent = Intent.createChooser(chooseFile, "Choose an APK");
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
    }

    public void moreInfo(View view)
    {
        if(apk_reponse_msg != null)
        {
            Intent feature_list_intent = new Intent(getApplicationContext(), ListActivity.class);
            JSONObject apk_info = null;
            try {
                apk_info = apk_reponse_msg.getJSONObject("info");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            feature_list_intent.putExtra("apk_info", apk_info.toString());
            feature_list_intent.putExtra("from_menu", true);
            startActivity(feature_list_intent);
        }
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
