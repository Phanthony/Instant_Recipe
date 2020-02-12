package com.phanthony.instantrecipe.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SpoonacularResult::class, RecipeInstruction::class, UserSettings::class], version = 1)
@TypeConverters(TypeConverterIngredient::class,TypeConverterStep::class)
abstract class RecipeDataBase: RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun settingsDao(): UserSettingsDao

    companion object{
        private var instance: RecipeDataBase? = null

        @Synchronized
        fun getInstance(context: Context): RecipeDataBase{
            synchronized(this){
                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,RecipeDataBase::class.java,"Recipe_Database"
                    ).build()
                    initialSetupDatabase(instance!!)
                }
            }
            return instance!!
        }
    }
}

fun initialSetupDatabase(recipeDataBase: RecipeDataBase){
    CoroutineScope(Dispatchers.IO).launch {
        val initSettings = UserSettings(0,1)
        recipeDataBase.settingsDao().insertSettings(initSettings)
    }
}