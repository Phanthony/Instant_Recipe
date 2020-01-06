package service

import database.SpoonacularResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularApi {

    @GET("ingredients/findByIngredients?number=100&ranking=2")
    fun getRecipes(@Query("ingredients", encoded = true) ingredients: String, @Query("apiKey") apiKey: String) : Call<List<SpoonacularResult>>
}