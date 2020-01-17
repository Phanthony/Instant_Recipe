package com.phanthony.instantrecipe.service

import com.phanthony.instantrecipe.database.RecipeInstruction
import com.phanthony.instantrecipe.database.SpoonacularResult
import io.reactivex.Single
import kotlin.Result as KtResult
import retrofit2.adapter.rxjava2.Result as RxResult


interface SpoonacularService {
    fun detectIngredients(texts: String): Single<KtResult<IngredientResults>>

    fun getRecipes(ingredients: String): Single<KtResult<List<SpoonacularResult>>>

    fun getRecipeInstructions(recipeId: Int): Single<KtResult<List<RecipeInstruction>>>

}