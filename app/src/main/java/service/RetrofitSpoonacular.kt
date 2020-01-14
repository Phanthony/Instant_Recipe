package service

import android.app.Application
import com.example.androidtraining.service.logger.AppActivityLogger
import database.RecipeInstruction
import database.SpoonacularResult
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitSpoonacular(application: Application): SpoonacularService {

    val spoonService: SpoonacularApi = Retrofit.Builder()
        .baseUrl("https://api.spoonacular.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .build()
        .create(SpoonacularApi::class.java)

    private val responseProcessor = ResponseProcessor(application, AppActivityLogger(), GsonJsonAdapter())



    override fun detectIngredients(texts: String): Single<Result<IngredientResults>> {
        return spoonService.detectIngredients(texts,"561d02ab93884e1eb9c633a623c27b92").map { result ->
            val processedResult = responseProcessor.process(result)
            val returnResult = if(processedResult.isSuccessful()){
                Result.success(processedResult.body!!)
            }else{
                Result.failure(processedResult.error!!)
            }
            returnResult
        }
    }

    override fun getRecipes(ingredients: String): Single<Result<List<SpoonacularResult>>> {
        return spoonService.getRecipes(ingredients, "561d02ab93884e1eb9c633a623c27b92").map { result ->
            val processedResult = responseProcessor.process(result)
            val returnResult = if(processedResult.isSuccessful()){
                Result.success(processedResult.body!!)
            } else{
                Result.failure(processedResult.error!!)
            }
            returnResult
        }
    }

    override fun getRecipeInstructions(recipeId: Int): Single<Result<List<RecipeInstruction>>> {
        return spoonService.getRecipeInstruction(recipeId, "561d02ab93884e1eb9c633a623c27b92").map { result ->
            val processedResult = responseProcessor.process(result)
            val returnResult = if(processedResult.isSuccessful()){
                Result.success(processedResult.body!!)
            }else{
                Result.failure(processedResult.error!!)
            }
            returnResult
        }
    }
}