package com.phanthony.instantrecipe.service

import com.phanthony.instantrecipe.database.RecipeInstruction
import com.phanthony.instantrecipe.database.SpoonacularResult
import io.reactivex.Single
import kotlin.Result as KtResult
import retrofit2.adapter.rxjava2.Result as RxResult

class RetrofitSpoonacular(private val spoonService: SpoonacularApi, private val responseProcessor: ResponseProcessor) :
    SpoonacularService {

    override fun detectIngredients(texts: String): Single<WrappedResult<IngredientResults>> {
        return spoonService.detectIngredients(texts, "561d02ab93884e1eb9c633a623c27b92").map { result ->
            val processedResult = responseProcessor.process(result)
            val returnResult = if (processedResult.isSuccessful()) {
                WrappedResult(KtResult.success(processedResult.body!!))
            } else {
                WrappedResult(KtResult.failure(processedResult.error!!))
            }
            returnResult
        }
    }

    override fun getRecipesService(ingredients: String): Single<WrappedResult<List<SpoonacularResult>>> {
        return spoonService.getRecipes(ingredients, "561d02ab93884e1eb9c633a623c27b92").map { result ->
            val processedResult = responseProcessor.process(result)
            val returnResult = if (processedResult.isFailure()) {
                WrappedResult(KtResult.failure(processedResult.error!!))
            } else {
                WrappedResult(KtResult.success(processedResult.body!!))
            }
            returnResult
        }
    }

    override fun getRecipeInstructions(recipeId: Int): Single<WrappedResult<List<RecipeInstruction>>> {
        return spoonService.getRecipeInstruction(recipeId, "561d02ab93884e1eb9c633a623c27b92").map { result ->
            val processedResult = responseProcessor.process(result)
            val returnResult = if (processedResult.isSuccessful()) {
                WrappedResult(KtResult.success(processedResult.body!!))
            } else {
                WrappedResult(KtResult.failure(processedResult.error!!))
            }
            returnResult
        }
    }
}