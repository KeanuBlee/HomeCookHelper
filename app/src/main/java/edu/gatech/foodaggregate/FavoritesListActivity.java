package edu.gatech.foodaggregate;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
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
 * Created by Param on 4/11/2017.
 */

public class FavoritesListActivity extends AppCompatActivity {
    CognitoUser user;
    DynamoDBMapper mapper;
    CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager client;
    ArrayList<String> ids;
    ArrayList<String> titles;
    TextView text;
    String[] recipeIds;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);
        ids = new ArrayList<>();
        titles = new ArrayList<>();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        CognitoUserPool userPool = new CognitoUserPool(getApplicationContext(), "us-east-1_ZPUPIylup", "72lqi02sjv18jsgom0722tmisa", "1opq49mtggjo0r2ul0jq6mtj2ej78ofph293pfb7kf53gvv7ee7f");
        user = userPool.getCurrentUser();
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // Context
                "us-east-1:654d88ee-2726-41e0-960f-3be89f3530db", // Identity Pool ID
                Regions.US_EAST_1); // Region
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
        StrictMode.setThreadPolicy(policy);
    }

    public void search(View view) {
        RadioGroup rg1 = (RadioGroup) findViewById(R.id.radioGroup);
        RadioGroup rg2 = (RadioGroup) findViewById(R.id.radioGroup2);
        final EditText search_query = (EditText) findViewById(R.id.search_query);
        final TextView textView = (TextView) findViewById(R.id.textView2);
        ArrayList<String> recipe_names = new ArrayList<>();
        ArrayList<String> recipe_ids = new ArrayList<>();
        if(rg2.getCheckedRadioButtonId() == R.id.internetRadio) {
            if (rg1.getCheckedRadioButtonId() == R.id.titleRadio) {
                try {
                    HttpResponse<JsonNode> response = Unirest.get("https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/search?instructionsRequired=false&limitLicense=false&number=10&offset=0&query="+search_query.getText().toString()+"&type=main+course")
                            .header("X-Mashape-Key", "DaiZsIaKDamshMHArSChvMzHFBZgp1yVdb7jsnizZgsd4WrFAp")
                            .header("Accept", "application/json")
                            .asJson();
                    JSONArray results_array = response.getBody().getObject().getJSONArray("results");
                    for(int i = 0; i < results_array.length(); i++) {

                        recipe_names.add(results_array.getJSONObject(i).getString("title"));
                        recipe_ids.add(results_array.getJSONObject(i).getString("id"));
                    }
                    Intent intent = new Intent(this, Search_List.class);
                    intent.putExtra("recipe_names", recipe_names);
                    intent.putExtra("recipe_ids", recipe_ids);
                    startActivity(intent);

                } catch (Exception e) {
                    textView.setText(e.toString());
                }
            } else {
                try {
                    // make spaces/commas between multiple ingredients '%2C'
                    String search_str = search_query.getText().toString().replace(",","%2C");
                    HttpResponse<JsonNode> response = Unirest.get("https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/findByIngredients?fillIngredients=false&ingredients="+search_str+"&limitLicense=false&number=10&ranking=1")
                            .header("X-Mashape-Key", "DaiZsIaKDamshMHArSChvMzHFBZgp1yVdb7jsnizZgsd4WrFAp")
                            .header("Accept", "application/json")
                            .asJson();
                    textView.setText(String.valueOf(response.getBody().getArray().length()));
                    JSONArray results_array = response.getBody().getArray();

                    for(int i = 0; i < results_array.length(); i++) {

                        recipe_names.add(results_array.getJSONObject(i).getString("title"));
                        recipe_ids.add(results_array.getJSONObject(i).getString("id"));
                    }

                    Intent intent = new Intent(this, Search_List.class);
                    intent.putExtra("recipe_names", recipe_names);
                    intent.putExtra("recipe_ids", recipe_ids);
                    startActivity(intent);

                } catch (Exception e) {
                        textView.setText(e.toString());
                }


            }
        } else {
            if (rg1.getCheckedRadioButtonId() == R.id.titleRadio) {
              /*  Thread mythread = new Thread() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {*/
                                Favorites favorites = mapper.load(Favorites.class, user.getUserId());
                                recipeIds = favorites.getRecipeID().split(",");
                                String query = favorites.getRecipeID().replace(",","%2C");
                                try {
                                    HttpResponse<JsonNode> response = Unirest.get("https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/informationBulk?ids="+query+"&includeNutrition=false")
                                            .header("X-Mashape-Key", "DaiZsIaKDamshMHArSChvMzHFBZgp1yVdb7jsnizZgsd4WrFAp")
                                            .header("Accept", "application/json")
                                            .asJson();

                                    JSONArray results_array = response.getBody().getArray();
                                    for(int i = 0; i < results_array.length(); i++) {
                                        if (results_array.getJSONObject(i).getString("title").toLowerCase().contains(search_query.getText().toString().toLowerCase())) {
                                            titles.add(results_array.getJSONObject(i).getString("title"));
                                            ids.add(results_array.getJSONObject(i).getString("id"));
                                        }
                                    }

                                } catch (Exception e) {
                                    textView.setText(e.toString());
                                }
                         /* }
                        });

                    }
                };*/
              /*  mythread.start();
                try {
                    mythread.join();
                } catch(Exception e) {

                }*/

              //  textView.setText(titles.size());
                Intent intent = new Intent(this, Search_List.class);
                intent.putExtra("recipe_names", titles);
                intent.putExtra("recipe_ids", ids);
                startActivity(intent);
            } else {

            }
        }
    }
}
