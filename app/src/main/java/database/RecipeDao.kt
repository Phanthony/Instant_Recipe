package database

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecipe(recipe: SpoonacularResult)

    @Query("DELETE FROM RECIPE_TABLE")
    fun clearRecipes()

    @Query("SELECT * FROM RECIPE_TABLE")
    fun getRecipe(): DataSource.Factory<Int,SpoonacularResult>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInstruction(instruction: RecipeInstruction)

    //@Query("SELECT * FROM Recipe_Steps_Table WHERE recipeId LIKE :id")
    //fun findInstruction(id:Int): List<RecipeInstruction>
}