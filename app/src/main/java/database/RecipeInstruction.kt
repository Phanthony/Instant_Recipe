package database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Recipe_Steps_Table")
class RecipeInstruction(var recipeId: Int?,
                        @PrimaryKey(autoGenerate = true) val id: Int,
                        @ColumnInfo val name: String,
                        @ColumnInfo val steps: List<RecipeSteps>)

