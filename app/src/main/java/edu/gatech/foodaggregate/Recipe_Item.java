package edu.gatech.foodaggregate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.regions.Regions;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by Keanu on 4/17/2017.
 */

public class Recipe_Item extends AppCompatActivity{
    private static String USER_FAVES_DATASET_NAME = "user_favorites";
    private Button favoriteButton;

    String id = "";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_item);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:654d88ee-2726-41e0-960f-3be89f3530db", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        CognitoSyncManager syncClient = new CognitoSyncManager(
                getApplicationContext(),
                Regions.US_EAST_1, // Region
                credentialsProvider);

        final Dataset dataset = syncClient.openOrCreateDataset(USER_FAVES_DATASET_NAME);
        favoriteButton = (Button) findViewById(R.id.button2);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataset.put("favorite", id);
                dataset.synchronize(new DefaultSyncCallback() {
                    @Override
                    public void onSuccess(Dataset dataset, List newRecords) {
                        //Your handler code here
                        Toast.makeText(Recipe_Item.this, "favorite added", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

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
            String directions_edit = directions_raw.replace('.','\n');
            directions.setText(directions_edit);
        } catch (Exception e) {

        }




    }
}
