package com.phanthony.instantrecipe.main

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.phanthony.instantrecipe.database.*
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.phanthony.instantrecipe.service.SpoonacularService
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.Result

val IDLE = 0
val SCANNING = 1
val FINISHED = 2

class RecipeViewModel(application: Application, db: RecipeDataBase, val service: SpoonacularService) :
    AndroidViewModel(application) {

    private val TAG = "TEXT_FOUND"
    private var settingsDao: UserSettingsDao = db.settingsDao()
    private var recipeDao: RecipeDao = db.recipeDao()
    private var recipeList: LiveData<PagedList<SpoonacularResult>?>? = null
    private var savedRecipeList: LiveData<PagedList<SpoonacularResult>>? = null
    private var selectedImage: Bitmap? = null
    private var ingList: MutableLiveData<MutableSet<String>> = MutableLiveData(mutableSetOf())
    private var imageQueue: ArrayList<Bitmap> = arrayListOf()
    private var currentRecipe: MutableLiveData<Int> = MutableLiveData()
    var scanning = MutableLiveData<Int>(IDLE)
    private var userSettings = settingsDao.getUserSettings()
    private var searchSettings = 0

    init {
        CoroutineScope(Dispatchers.IO).launch {
            initialSetup()
        }
    }

    fun observeIngList(context: LifecycleOwner){
        ingList.observe(context, Observer { ingSet ->
            val factory: DataSource.Factory<Int, SpoonacularResult> = recipeDao.getRecipe(ingSet.toString())
            recipeList = LivePagedListBuilder(factory, 10).build()
        })
    }

    @SuppressLint("CheckResult")
    fun changeSearchSettings(code: Int){
        settingsDao.getSingleSetting().subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe { settings ->
            settings.ingredientSearch = code
        }
    }

    fun observeSettings(context: LifecycleOwner){
        userSettings.observe(context, Observer { settings ->
            searchSettings = settings.ingredientSearch
        })
    }

    fun initialSetup() {
        val savedRecipeFactory: DataSource.Factory<Int, SpoonacularResult>? = recipeDao.getSavedRecipes()
        if(savedRecipeFactory!=null){
            savedRecipeList = LivePagedListBuilder(savedRecipeFactory, 10).build()
        }
    }

    fun observeQueue(context: LifecycleOwner) {
        scanning.observe(context, Observer { status ->
            when (status) {
                SCANNING -> {
                    imageQueue.removeAt(0)
                }
                IDLE -> {
                    if (imageQueue.isNotEmpty()) {
                        runTextRecognition()
                    }
                }
            }
        })
    }

    fun setUpSet(set: MutableSet<String>): String {
        var builder = ""
        var first = true
        set.forEach { ing ->
            if (first) {
                builder += ing.replace(" ", "+")
                first = false
            } else {
                builder += ",+${ing.replace(" ", "+")}"
            }
        }
        return builder
    }

    fun getRecipeInstruction(recipeId: Int): Single<Result<Int>> {
        return service.getRecipeInstructions(recipeId).observeOn(Schedulers.io())
            .map { res ->
                val result = res.result
                val networkResult = if (result.isFailure) {
                    val error = result.exceptionOrNull()!!
                    Result.failure(error)
                } else {
                    val resultBody = result.getOrNull()!!
                    if (resultBody.isNotEmpty()) {
                        resultBody.forEach {
                            it.recipeId = recipeId
                        }
                        recipeDao.insertInstruction(resultBody)
                        Result.success(1)
                    } else {
                        // No instructions for this recipe
                        Result.success(2)
                    }
                }
                networkResult
            }
    }

    fun insertRecipe(recipe: SpoonacularResult){
        recipeDao.insertRecipe(recipe)
    }

    @SuppressLint("CheckResult")
    fun seeRecipeSaved(recipeId: Int): Maybe<Result<Boolean>>{
        return getSingleRecipe(recipeId).map { singleRecipe ->
            Result.success(singleRecipe.saved)
        }
    }

    fun getRecipes(set: MutableSet<String>): Single<Result<Int>> {
        val ingredients = setUpSet(set)
        return service.getRecipes(ingredients,searchSettings)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map { res ->
                val processedResult = res.result
                val networkResult = if (processedResult.isFailure) {
                    val error = processedResult.exceptionOrNull()!!
                    Result.failure(error)
                } else {
                    val resultBody = processedResult.getOrNull()!!
                    if (resultBody.isNotEmpty()) {
                        val ingIndentifier = ingList.value!!.toString()
                        resultBody.forEach {
                            seeRecipeSaved(it.id).subscribe { savedResult -> // Check if the recipe is in user's saved recipes
                                it.saved = savedResult.getOrNull()!!
                            }
                            it.ingIdentifier = ingIndentifier
                            recipeDao.insertRecipe(it)
                        }
                        Result.success(1)
                    } else {
                        Result.success(2)
                    }
                }
                networkResult
            }
    }



    fun addImageToQueue(image: Bitmap) {
        imageQueue.add(image)
        if (scanning.value != SCANNING) {
            runTextRecognition()
        }
    }

    fun setImage(image: Bitmap) {
        selectedImage = image
    }

    fun getImage(): Bitmap {
        return selectedImage!!
    }

    fun setIngList(set: MutableSet<String>) {
        val newSet = ingList.value!!
        newSet.addAll(set)
        ingList.value = newSet
    }

    fun getIngList() = ingList

    fun getRecipeList() = recipeList

    fun getSavedRecipeList() = savedRecipeList

    fun setScanning(value: Int) {
        scanning.value = value
    }

    fun setRecipe(value: Int){
        currentRecipe.value = value
    }

    fun getRecipe(): MutableLiveData<Int>{
        return currentRecipe
    }

    fun getSingleRecipe(recipeId: Int): Maybe<SpoonacularResult> {
        return recipeDao.getRecipeSingle(recipeId)
    }

    fun getRecipeInstructions(recipeId: Int): Single<List<RecipeInstruction>>{
        return recipeDao.findInstruction(recipeId)
    }

    fun setRecipeSaveAndSeen(recipe: SpoonacularResult) {
        recipe.saved = true
        recipe.seen = true
    }

    fun setRecipeSeen(recipe: SpoonacularResult) {
        recipe.seen = true
    }

    fun setRecipeUnsave(recipe: SpoonacularResult){
        recipe.saved = false
    }

    @SuppressLint("CheckResult")
    fun changeRecipeInformation(recipeId: Int, change: (SpoonacularResult) -> Unit) {
        getSingleRecipe(recipeId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe { singleRecipe ->
                change(singleRecipe)
                insertRecipe(singleRecipe)
            }
    }


    fun runTextRecognition() {
        val firebaseImage = FirebaseVisionImage.fromBitmap(imageQueue[0])
        setScanning(SCANNING)
        val recognizer = FirebaseVision.getInstance()
            .onDeviceTextRecognizer
        recognizer.processImage(firebaseImage).addOnCompleteListener { result ->
            if (result.isSuccessful) {
                detectIngredients(result.result!!.text)
            } else {
                setScanning(FINISHED)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun detectIngredients(texts: String) {
        val text = texts.toLowerCase().replace("\n", " ")
        val ingredientList = mutableSetOf<String>()
        service.detectIngredients(text)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { res ->
                val result = res.result
                if (result.isFailure) {
                    val error = result.exceptionOrNull()!!
                    // Throw error display
                } else {
                    val resultBody = result.getOrNull()!!
                    if (resultBody.annotations.isNotEmpty()) {
                        resultBody.annotations.forEach {
                            ingredientList.add(it.annotation)
                        }
                    } else {
                        // No ingredients found
                    }
                }
                Log.i(TAG, ingredientList.toString())
                setIngList(ingredientList)
                setScanning(FINISHED)
            }
    }
}