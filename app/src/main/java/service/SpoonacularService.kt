package service

import android.content.Context
import database.RecipeInstruction
import database.SpoonacularResult
import io.reactivex.Observable
import io.reactivex.Single

interface SpoonacularService {
    fun detectIngredients(texts: String): Single<Result<IngredientResults>>

    fun getRecipes(ingredients: String): Single<Result<List<SpoonacularResult>>>

    fun getRecipeInstructions(recipeId: Int): Single<Result<List<RecipeInstruction>>>

}