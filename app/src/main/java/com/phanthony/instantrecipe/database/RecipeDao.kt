package com.phanthony.instantrecipe.database

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecipe(recipe: SpoonacularResult)

    @Query("DELETE FROM RECIPE_TABLE")
    fun clearRecipes()

    @Query("SELECT * FROM RECIPE_TABLE WHERE ingIdentifier LIKE :ingIdentifier")
    fun getRecipe(ingIdentifier: String): DataSource.Factory<Int,SpoonacularResult>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInstruction(instructionList: List<RecipeInstruction>)

    @Query("SELECT * FROM Recipe_Table WHERE id LIKE :id")
    fun getRecipeSingle(id: Int): Maybe<SpoonacularResult>

    @Query("SELECT * FROM Recipe_Steps_Table WHERE recipeId LIKE :id")
    fun findInstruction(id:Int): Single<List<RecipeInstruction>>

    // True is mapped to 1 while false is mapped to 0 in room
    // https://stackoverflow.com/questions/47730820/hardcode-boolean-query-in-room-database
    @Query("SELECT * FROM RECIPE_TABLE WHERE saved = 1")
    fun getSavedRecipes(): DataSource.Factory<Int, SpoonacularResult>
}