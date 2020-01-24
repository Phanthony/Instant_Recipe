package com.phanthony.instantrecipe.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Recipe_Steps_Table")
class RecipeInstruction(@PrimaryKey var recipeId: Int?,
                        @ColumnInfo val name: String,
                        @ColumnInfo val steps: List<RecipeSteps>)

