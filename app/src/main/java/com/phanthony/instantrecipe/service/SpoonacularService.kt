package com.phanthony.instantrecipe.service

import com.phanthony.instantrecipe.database.RecipeInstruction
import com.phanthony.instantrecipe.database.SpoonacularResult
import io.reactivex.Single

interface SpoonacularService {
    fun detectIngredients(texts: String): Single<Result<IngredientResults>>

    fun getRecipes(ingredients: String): Single<Result<List<SpoonacularResult>>>

    fun getRecipeInstructions(recipeId: Int): Single<Result<List<RecipeInstruction>>>

}