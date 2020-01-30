package com.phanthony.instantrecipe.service

import com.phanthony.instantrecipe.database.RecipeInstruction
import com.phanthony.instantrecipe.database.SpoonacularResult
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result as RxResult
import kotlin.Result as KtResult
import retrofit2.http.*

interface SpoonacularApi {

    @GET("recipes/findByIngredients?number=100&ignorePantry=true&ranking=1")
    fun getRecipes(@Query("ingredients", encoded = true) ingredients: String, @Query("apiKey") apiKey: String)
            : Single<RxResult<List<SpoonacularResult>>>

    @GET("recipes/{recipeId}/analyzedInstructions")
    fun getRecipeInstruction(@Path("recipeId") recipeId: Int, @Query("apiKey") apiKey: String)
            : Single<RxResult<List<RecipeInstruction>>>

    @FormUrlEncoded
    @POST("food/detect")
    fun detectIngredients(@Field("text") text: String, @Query("apiKey") apiKey: String)
            : Single<RxResult<IngredientResults>>

}
