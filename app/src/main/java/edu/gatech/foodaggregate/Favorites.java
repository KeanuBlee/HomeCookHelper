package edu.gatech.foodaggregate;

/**
 * Created by Keanu on 4/20/2017.
 */
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "Favorites")
public class Favorites {
    private String userName;
    private String recipeID;

    @DynamoDBIndexRangeKey(attributeName = "RecipeID")
    public String getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(String id) {
        this.recipeID = id;
    }

    @DynamoDBHashKey(attributeName = "UserName")
        public String getUserName() {
            return userName;
        }
        public void setUserName(String userName) {
            this.userName =  userName;
        }

}
