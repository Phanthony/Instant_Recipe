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
import retrofit2.converter.moshi.MoshiConverterFactory
import service.SpoonacularApi

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
    private var ingredientMap: HashMap<String, Int>? = null
    private var imageQueue: ArrayList<Bitmap> = arrayListOf()
    var scanning = MutableLiveData<Int>(IDLE)
    val spoonService: SpoonacularApi = Retrofit.Builder()
        .baseUrl("https://api.spoonacular.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
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

    fun getRecipes(context: Context){
        val list = spoonService.getRecipes("apples","561d02ab93884e1eb9c633a623c27b92")
        list.enqueue(object : Callback<List<SpoonacularResult>>{
            override fun onResponse(call: Call<List<SpoonacularResult>>, response: Response<List<SpoonacularResult>>) {
                CoroutineScope(Dispatchers.IO).launch {
                    recipeDao.clearRecipes()
                    response.body()?.forEach { recipe -> recipeDao.insertRecipe(recipe) }
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

    fun setMap(map: HashMap<String, Int>) {
        ingredientMap = map
    }

    fun getMap(): HashMap<String, Int> {
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
                processTextRecognitionResult(result.result!!)
            } else {
                setScanning(FINISHED)
            }
        }
    }

    fun findIngredient(text: String): ArrayList<String> {
        val skipList = arrayListOf("to", "of", "back", "sour", "live", "store", "on", "n", "lb")
        val ingredientList = ingredientMap!!.keys
        val temp = text.toLowerCase()
        val builderList = arrayListOf<String>()
        if (!skipList.contains(temp) && !temp.contains(Regex("\\W"))) {
            val regexBuilder = "\\b($temp)\\b"
            for (ingredient in ingredientList) {
                if (ingredient.contains(Regex(regexBuilder))) {
                    builderList.add(ingredient)
                }
            }
        }

        return builderList
    }

    private fun processTextRecognitionResult(texts: FirebaseVisionText) {
        val recipeList = mutableSetOf<String>()
        val blocks = texts.textBlocks
        if (blocks.isEmpty()) {
        } else {
            for (i in 0 until blocks.size) {
                val lines = blocks[i].lines
                for (j in 0 until lines.size) {
                    val elements = lines[j].elements
                    for (k in 0 until elements.size) {
                        val check = findIngredient(elements[k].text)
                        for (found in check) {
                            recipeList.add(found)
                        }
                    }
                }
            }
        }
        Log.i(TAG, recipeList.toString())
        setIngList(recipeList)
        setScanning(FINISHED)
    }
}