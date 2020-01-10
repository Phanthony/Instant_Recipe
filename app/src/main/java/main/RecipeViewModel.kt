package main

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.phanthony.instantrecipe.R
import database.RecipeInstruction
import database.SpoonacularResult
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import service.IngredientResults
import service.SpoonacularApi
import service.textBody

val IDLE = 0
val SCANNING = 1
val FINISHED = 2

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "TEXT_FOUND"

    private var db = database.RecipeDataBase.getInstance(getApplication())
    private var recipeDao = db.recipeDao()
    private lateinit var recipeList: LiveData<PagedList<SpoonacularResult>?>
    private var selectedImage: Bitmap? = null
    private var ingList: MutableLiveData<MutableSet<String>> = MutableLiveData(mutableSetOf())
    private var ingredientMap: HashMap<String, String>? = null
    private var imageQueue: ArrayList<Bitmap> = arrayListOf()
    var scanning = MutableLiveData<Int>(IDLE)
    val spoonService: SpoonacularApi = Retrofit.Builder()
        .baseUrl("https://api.spoonacular.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpoonacularApi::class.java)

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
        val list = spoonService.getRecipeInstruction(recipeId, "561d02ab93884e1eb9c633a623c27b92")
        list.enqueue(object : Callback<List<RecipeInstruction>> {
            override fun onFailure(call: Call<List<RecipeInstruction>>, t: Throwable) {
            }

            override fun onResponse(call: Call<List<RecipeInstruction>>, response: Response<List<RecipeInstruction>>) {
                if (response.body() != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        response.body()!!.forEach {
                            it.recipeId = recipeId
                            recipeDao.insertInstruction(it)
                        }
                    }
                }
            }

        })
    }

    fun getRecipes(context: Context, set: MutableSet<String>) {
        val ingredients = setUpSet(set)
        val list = spoonService.getRecipes(ingredients, "561d02ab93884e1eb9c633a623c27b92")
        list.enqueue(object : Callback<List<SpoonacularResult>> {
            override fun onResponse(call: Call<List<SpoonacularResult>>, response: Response<List<SpoonacularResult>>) {
                if (response.body() != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        recipeDao.clearRecipes()
                        response.body()?.forEach { recipe -> recipeDao.insertRecipe(recipe) }
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.no_recipe), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<SpoonacularResult>>, t: Throwable) {
                Toast.makeText(context, context.getString(R.string.no_recipe), Toast.LENGTH_LONG).show()
            }
        })
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
                //processTextRecognitionResult(result.result!!)
                detectIngredients(result.result!!)
            } else {
                setScanning(FINISHED)
            }
        }
    }

    private fun detectIngredients(texts: FirebaseVisionText){
        val ingredientList = mutableSetOf<String>()
        val text = texts.text.toLowerCase().replace("\n"," ")
        spoonService.detectIngredients(text,"561d02ab93884e1eb9c633a623c27b92").enqueue(object : Callback<IngredientResults>{
            override fun onFailure(call: Call<IngredientResults>, t: Throwable) {
                Log.i("ERROR",t.message)
            }

            override fun onResponse(call: Call<IngredientResults>, response: Response<IngredientResults>) {
                if(response.body()!= null){
                    response.body()!!.annotations.forEach { ingredientList.add(it.annotation) }
                }
                else{
                    // No ingredients found
                }
                Log.i(TAG, ingredientList.toString())
                setIngList(ingredientList)
                setScanning(FINISHED)
            }

        })
    }
}