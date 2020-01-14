package com.phanthony.instantrecipe.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.phanthony.instantrecipe.database.RecipeDataBase
import com.phanthony.instantrecipe.service.RetrofitSpoonacular

class RecipeViewModelFactory(private val application: Application): ViewModelProvider.AndroidViewModelFactory(application) {

    val db = RecipeDataBase.getInstance(application)
    val service = RetrofitSpoonacular(application)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RecipeViewModel(application,db,service) as T
    }
}