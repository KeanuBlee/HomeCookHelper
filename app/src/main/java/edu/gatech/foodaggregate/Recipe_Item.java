package edu.gatech.foodaggregate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.amazonaws.services.dynamodbv2.model.*;
import org.json.JSONArray;
import org.w3c.dom.Text;

import mobile.AWSMobileClient;

/**
 * Created by Keanu on 4/17/2017.
 */

public class Recipe_Item extends AppCompatActivity{
    String id = "";
    CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager client;
    Dataset dataset;
    Button fav_button;
    CognitoUser user;
    DynamoDBMapper mapper;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_item);
        fav_button = (Button)findViewById(R.id.favorite_button);
        CognitoUserPool userPool = new CognitoUserPool(getApplicationContext(), "us-east-1_ZPUPIylup", "72lqi02sjv18jsgom0722tmisa", "1opq49mtggjo0r2ul0jq6mtj2ej78ofph293pfb7kf53gvv7ee7f");
         user = userPool.getCurrentUser();
         credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // Context
                "us-east-1:654d88ee-2726-41e0-960f-3be89f3530db", // Identity Pool ID
                Regions.US_EAST_1); // Region
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
  ///       client = new CognitoSyncManager(getApplicationContext(),Regions.US_EAST_1,credentialsProvider);
//         dataset = client.openOrCreateDataset("Favorites");
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
       /* Runnable runnable = new Runnable() {
            @Override
            public void run() {
                    Favorites favorites = new Favorites();
                    favorites.setRecipeID(id);
                    favorites.setUserName(user.getUserId());
                    mapper.save(favorites);
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();*/


        TextView title = (TextView) findViewById(R.id.title);
        TextView ingredients = (TextView) findViewById(R.id.ingredients);
        TextView directions = (TextView) findViewById(R.id.directions);

        try {
            HttpResponse<JsonNode> response = Unirest.get("https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/"+id+"/information?includeNutrition=false")
                    .header("X-Mashape-Key", "DaiZsIaKDamshMHArSChvMzHFBZgp1yVdb7jsnizZgsd4WrFAp")
                    .header("Accept", "application/json")
                    .asJson();
            String ingredient_list = "";
            JSONArray ingredientsArr = response.getBody().getObject().getJSONArray("extendedIngredients");
            title.setText(response.getBody().getObject().get("title").toString());
            for (int i = 0; i < ingredientsArr.length(); i++) {
                ingredient_list = ingredient_list + ingredientsArr.getJSONObject(i).get("name").toString();
                ingredient_list = ingredient_list + " ";
                ingredient_list = ingredient_list + ingredientsArr.getJSONObject(i).get("amount").toString() + " " + ingredientsArr.getJSONObject(i).get("unit");
                ingredient_list = ingredient_list + "\n";
            }
                    ingredients.setText(ingredient_list);

            String directions_raw = response.getBody().getObject().get("instructions").toString();
            String directions_edit = directions_raw.replace(".","\n\n");
            directions.setText(directions_edit);
        } catch (Exception e) {

        }
    }

    public void favorite(View view) {
        //"us-east-1:654d88ee-2726-41e0-960f-3be89f3530db"
        //CognitoUserPool userPool = new CognitoUserPool(getApplicationContext(), "us-east-1_ZPUPIylup", "72lqi02sjv18jsgom0722tmisa", "1opq49mtggjo0r2ul0jq6mtj2ej78ofph293pfb7kf53gvv7ee7f");
        //CognitoUser user = userPool.getCurrentUser();
      /*  CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // Context
                "us-east-1:654d88ee-2726-41e0-960f-3be89f3530db", // Identity Pool ID
                Regions.US_EAST_1); // Region
        CognitoSyncManager client = new CognitoSyncManager(getApplicationContext(),Regions.US_EAST_1,credentialsProvider);*/
        //Dataset dataset = client.openOrCreateDataset("Favorites");
        String data;

        Thread mythread = new Thread() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Favorites favorites = mapper.load(Favorites.class, user.getUserId());
                        String data = favorites.getRecipeID();
                        if (fav_button.getText().equals("Favorite")){
                            if (!data.contains(id)) {
                                if (data.isEmpty()) {
                                    data = data + id;
                                } else {
                                    data = data + "," + id;
                                }
                            }
                            fav_button.setText("Unfavorite");
                        } else {
                            if (data.contains("," + id + ",")) {
                                data = data.replace("," + id + ",", ",");
                            } else if(data.contains(id + ",")) {
                                data = data.replace(id + ",", "");
                            } else if(data.contains("," + id)) {
                                data =data.replace("," + id, "");
                            }
                            fav_button.setText("Favorite");
                        }
                        favorites.setRecipeID(data);
                        mapper.save(favorites);
                    }
                });

            }
        };
        mythread.start();
      //  TextView text = (TextView) findViewById(R.id.title);
       /* if (fav_button.getText().equals("Favorite")) {
            if (data.isEmpty()) {
                dataset.put("RecipeIDs", id);
            } else {
                dataset.put("RecipeIDs", data + "," + id);
            }
            fav_button.setText("Unfavorite");
        } else {
           /* if (data.contains(",") == false) {
                data.replace(id,"");
            } else {
                if (data.contains("," + id + ",")) {
                    data.replace("," + id + ",", ",");
                } else if(data.contains(id + ",")) {
                    data.replace(id + ",", "");
                } else if(data.contains("," + id)) {
                    data.replace("," + id, "");

                }
            }
            dataset.put("RecipeIDs",data);
         *///   fav_button.setText("Favorite");
 //       }
     //   text.setText(data);

       // dataset.synchronize(new DefaultSyncCallback());


    }
}
