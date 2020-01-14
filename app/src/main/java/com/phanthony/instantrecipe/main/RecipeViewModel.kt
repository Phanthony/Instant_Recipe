package com.phanthony.instantrecipe.main

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.phanthony.instantrecipe.database.RecipeDataBase
import com.phanthony.instantrecipe.database.SpoonacularResult
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.phanthony.instantrecipe.service.SpoonacularService

val IDLE = 0
val SCANNING = 1
val FINISHED = 2

class RecipeViewModel(application: Application, db: RecipeDataBase, val service: SpoonacularService) :
    AndroidViewModel(application) {

    private val TAG = "TEXT_FOUND"
    private var recipeDao = db.recipeDao()
    private lateinit var recipeList: LiveData<PagedList<SpoonacularResult>?>
    private var selectedImage: Bitmap? = null
    private var ingList: MutableLiveData<MutableSet<String>> = MutableLiveData(mutableSetOf())
    private var ingredientMap: HashMap<String, String>? = null
    private var imageQueue: ArrayList<Bitmap> = arrayListOf()
    var scanning = MutableLiveData<Int>(IDLE)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val factory: DataSource.Factory<Int, SpoonacularResult> = recipeDao.getRecipe()
            recipeList = LivePagedListBuilder(factory, 10).build()
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

    fun getRecipeInstruction(recipeId: Int) {
        service.getRecipeInstructions(recipeId)
            .subscribeOn(Schedulers.io())
            .subscribe { result ->
                if (result.isFailure) {
                    val error = result.exceptionOrNull()!!
                    // Throw error display

                } else {
                    val resultBody = result.getOrNull()!!
                    if (resultBody.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            resultBody.forEach {
                                it.recipeId = recipeId
                                recipeDao.insertInstruction(it)
                            }
                        }
                    } else {
                        // No instructions for this recipe
                    }
                }

            }
    }

    fun getRecipes(set: MutableSet<String>): Single<Result<Int>> {
        val ingredients = setUpSet(set)
        return service.getRecipes(ingredients).map { result ->
            val networkResult = if (result.isFailure) {
                val error = result.exceptionOrNull()!!
                Result.failure(error)
            } else {
                val resultBody = result.getOrNull()!!
                if (resultBody.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        recipeDao.clearRecipes()
                        resultBody.forEach {
                            recipeDao.insertRecipe(it)
                        }
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

    fun setMap(map: HashMap<String, String>) {
        ingredientMap = map
    }

    fun getMap(): HashMap<String, String> {
        return ingredientMap!!
    }

    fun setIngList(set: MutableSet<String>) {
        val newSet = ingList.value!!
        newSet.addAll(set)
        ingList.value = newSet
    }

    fun getIngList() = ingList

    fun getRecipeList() = recipeList

    fun setScanning(value: Int) {
        scanning.value = value
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

    private fun detectIngredients(texts: String) {
        val text = texts.toLowerCase().replace("\n", " ")
        val ingredientList = mutableSetOf<String>()
        service.detectIngredients(text)
            .subscribeOn(Schedulers.io())
            .subscribe { result ->
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
                CoroutineScope(Dispatchers.Main).launch {
                    Log.i(TAG, ingredientList.toString())
                    setIngList(ingredientList)
                    setScanning(FINISHED)
                }
            }
    }
}