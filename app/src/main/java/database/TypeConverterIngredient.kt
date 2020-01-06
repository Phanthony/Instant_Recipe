package database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class TypeConverterIngredient {

    val gson = GsonBuilder()
        //.registerTypeAdapter(Ingredient::class.java, IngredientInterfaceAdapter())
        .create()

    @TypeConverter
    fun stringToIngredientList(data: String?): List<Ingredient>{
        if (data == null){
            return listOf()
        }
        val listIngredientType = object : TypeToken<List<Ingredient>>(){}.type
        return gson.fromJson(data,listIngredientType)
    }

    @TypeConverter
    fun ingredientListToString(list: List<Ingredient>): String{
        return gson.toJson(list)
    }
}