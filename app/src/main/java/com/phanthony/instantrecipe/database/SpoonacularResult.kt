package com.phanthony.instantrecipe.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Recipe_Table")
data class SpoonacularResult(@PrimaryKey val id: Int,
                             @ColumnInfo val image: String,
                             @ColumnInfo val likes: Int,
                             @ColumnInfo val missedIngredients: List<Ingredient>,
                             @ColumnInfo val title: String,
                             @ColumnInfo val usedIngredients: List<Ingredient>)