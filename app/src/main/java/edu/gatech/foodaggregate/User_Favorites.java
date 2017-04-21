package edu.gatech.foodaggregate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by Keanu on 4/20/2017.
 */

public class User_Favorites extends AppCompatActivity implements AdapterView.OnItemClickListener {
    CognitoUser user;
    DynamoDBMapper mapper;
    CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager client;
    ArrayList<String> ids;
    ArrayList<String> titles;
    TextView text;
    String[] recipeIds;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_favorites);

        CognitoUserPool userPool = new CognitoUserPool(getApplicationContext(), "us-east-1_ZPUPIylup", "72lqi02sjv18jsgom0722tmisa", "1opq49mtggjo0r2ul0jq6mtj2ej78ofph293pfb7kf53gvv7ee7f");
        user = userPool.getCurrentUser();
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // Context
                "us-east-1:654d88ee-2726-41e0-960f-3be89f3530db", // Identity Pool ID
                Regions.US_EAST_1); // Region
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
        ids = new ArrayList<>();
        titles = new ArrayList<>();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Favorites favorites = mapper.load(Favorites.class, user.getUserId());
                        recipeIds = favorites.getRecipeID().split(",");
                        String query = favorites.getRecipeID().replace(",", "%2C");
                        HttpResponse<JsonNode> response = Unirest.get("https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/informationBulk?ids=" + query + "&includeNutrition=false")
                                .header("X-Mashape-Key", "DaiZsIaKDamshMHArSChvMzHFBZgp1yVdb7jsnizZgsd4WrFAp")
                                .header("Accept", "application/json")
                                .asJson();

                        JSONArray results_array = response.getBody().getArray();
                        for (int i = 0; i < results_array.length(); i++) {

                            titles.add(results_array.getJSONObject(i).getString("title"));
                            ids.add(results_array.getJSONObject(i).getString("id"));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();

        try {
            mythread.join();
        } catch(Exception e) {

        }
        try {
            ListView lv = (ListView) findViewById(R.id.favorite_list);
            lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles));
            lv.setOnItemClickListener(this);
        } catch(Exception e) {}

    }
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        //finds the item clicked and passes its' data into directions.
        String data = (String) l.getItemAtPosition(position);
        Intent intent = new Intent(this, Recipe_Item.class);
        intent.putExtra("id", ids.get(titles.indexOf(data)));
        startActivity(intent);

    }
}
