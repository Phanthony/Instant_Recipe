package com.phanthony.instantrecipe.service

import com.phanthony.instantrecipe.database.RecipeInstruction
import com.phanthony.instantrecipe.database.SpoonacularResult
import io.reactivex.Single
import kotlin.Result as KtResult
import retrofit2.adapter.rxjava2.Result as RxResult


interface SpoonacularService {
    fun detectIngredients(texts: String): Single<WrappedResult<IngredientResults>>

    fun getRecipes(ingredients: String, ranking: Int): Single<WrappedResult<List<SpoonacularResult>>>

    fun getRecipeInstructions(recipeId: Int): Single<WrappedResult<List<RecipeInstruction>>>

}