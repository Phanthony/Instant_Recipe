package com.phanthony.instantrecipe.database

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecipe(recipe: SpoonacularResult)

    @Query("DELETE FROM RECIPE_TABLE")
    fun clearRecipes()

    @Query("SELECT * FROM RECIPE_TABLE")
    fun getRecipe(): DataSource.Factory<Int,SpoonacularResult>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInstruction(instructionList: List<RecipeInstruction>)

    @Query("SELECT * FROM Recipe_Table WHERE id lIKE :id")
    fun getRecipeSingle(id: Int): Single<SpoonacularResult>

    @Query("SELECT * FROM Recipe_Steps_Table WHERE recipeId LIKE :id")
    fun findInstruction(id:Int): Single<List<RecipeInstruction>?>
}