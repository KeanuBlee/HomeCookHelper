package edu.gatech.foodaggregate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import org.json.JSONArray;
import org.w3c.dom.Text;

/**
 * Created by Keanu on 4/17/2017.
 */

public class Recipe_Item extends AppCompatActivity{
    String id = "";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_item);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");

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
