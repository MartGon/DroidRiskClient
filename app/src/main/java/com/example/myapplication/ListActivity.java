package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ListActivity extends AppCompatActivity {

    // Constants

    // Widgets
    ListView item_list_view;
    ArrayAdapter<String> adapter;

    // Logic
    ArrayList<String> item_list;
    JSONObject apk_info;
    boolean from_menu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Set interface items
        item_list_view = findViewById(R.id.item_list);

        SetupList();

        // Setup listener if first level
        if(from_menu)
        {
            item_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    ClickItem(position);
                }
            });
        }
    }

    public void SetupList()
    {
        from_menu = getIntent().getExtras().getBoolean("from_menu");
        item_list = new ArrayList<>();

        if(from_menu)
        {
            try {
                apk_info = new JSONObject(getIntent().getExtras().getString("apk_info"));

                for (Iterator<String> i = apk_info.keys(); i.hasNext(); ) {
                    String item = i.next();
                    item_list.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            try
            {
                String key = getIntent().getExtras().getString("key");
                JSONObject info_list_json = new JSONObject(getIntent().getExtras().getString("apk_info"));
                JSONArray info_list = info_list_json.getJSONArray(key);

                for(int i = 0; i < info_list.length(); i++)
                    item_list.add(info_list.getString(i));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, item_list);
        item_list_view.setAdapter(adapter);
    }

    public void ClickItem(int index)
    {
        if (index >= item_list.size())
            return;

        String key = item_list.get(index);
        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
        intent.putExtra("apk_info", apk_info.toString());
        intent.putExtra("key", key);
        intent.putExtra("from_menu", false);
        startActivity(intent);
    }
}
