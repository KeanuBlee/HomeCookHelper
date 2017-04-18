package edu.gatech.foodaggregate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Keanu on 4/17/2017.
 */

public class Search_List extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ArrayList<String> recipe_names;
    ArrayList<String> recipe_ids;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_list);
        Intent intent = getIntent();

        recipe_names = intent.getStringArrayListExtra("recipe_names");
        recipe_ids = intent.getStringArrayListExtra("recipe_ids");

        ListView lv = (ListView) findViewById(R.id.recipe_list);
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, recipe_names));
        lv.setOnItemClickListener(this);
    }



    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        //finds the item clicked and passes its' data into directions.
        String data = (String) l.getItemAtPosition(position);
        Intent intent = new Intent(this, Recipe_Item.class);
        intent.putExtra("id", recipe_ids.get(recipe_names.indexOf(data)));
        startActivity(intent);

    }
}

