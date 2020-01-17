package com.phanthony.instantrecipe.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidtraining.service.logger.AppActivityLogger
import com.phanthony.instantrecipe.database.RecipeDataBase
import com.phanthony.instantrecipe.service.MoshiJsonAdapter
import com.phanthony.instantrecipe.service.ResponseProcessor
import com.phanthony.instantrecipe.service.RetrofitSpoonacular
import com.phanthony.instantrecipe.service.SpoonacularApi
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class RecipeViewModelFactory(private val application: Application): ViewModelProvider.AndroidViewModelFactory(application) {

    val spoonService: SpoonacularApi = Retrofit.Builder()
        .baseUrl("https://api.spoonacular.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .build()
        .create(SpoonacularApi::class.java)

    private val responseProcessor = ResponseProcessor(application, AppActivityLogger(), MoshiJsonAdapter())

    val db = RecipeDataBase.getInstance(application)
    val service = RetrofitSpoonacular(spoonService,responseProcessor)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RecipeViewModel(application,db,service) as T
    }
}