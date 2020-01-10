package service

import database.RecipeInstruction
import database.SpoonacularResult
import retrofit2.Call
import retrofit2.http.*

interface SpoonacularApi {

    @GET("recipes/findByIngredients?number=50&ranking=2")
    fun getRecipes(@Query("ingredients", encoded = true) ingredients: String, @Query("apiKey") apiKey: String) : Call<List<SpoonacularResult>>

    @GET("recipes/{recipeId}/analyzedInstructions")
    fun getRecipeInstruction(@Path("recipeId") recipeId: Int, @Query("apiKey") apiKey: String): Call<List<RecipeInstruction>>

    @FormUrlEncoded
    @POST("food/detect")
    fun detectIngredients(@Field("text") text: String, @Query("apiKey") apiKey: String): Call<IngredientResults>

}

data class textBody(val text: String)