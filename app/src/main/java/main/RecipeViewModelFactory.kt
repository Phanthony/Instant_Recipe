package main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import database.RecipeDataBase
import service.RetrofitSpoonacular

class RecipeViewModelFactory(private val application: Application): ViewModelProvider.AndroidViewModelFactory(application) {

    val db = RecipeDataBase.getInstance(application)
    val service = RetrofitSpoonacular(application)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RecipeViewModel(application,db,service) as T
    }
}