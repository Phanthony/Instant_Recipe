package database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SpoonacularResult::class, RecipeInstruction::class], version = 1)
@TypeConverters(TypeConverterIngredient::class,TypeConverterStep::class)
abstract  class RecipeDataBase: RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object{
        private var instance: RecipeDataBase? = null

        @Synchronized
        fun getInstance(context: Context): RecipeDataBase{
            synchronized(this){
                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,RecipeDataBase::class.java,"Recipe_Database"
                    ).build()
                }
            }
            return instance!!
        }
    }

}