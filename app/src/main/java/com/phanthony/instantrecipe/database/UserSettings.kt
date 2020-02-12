package com.phanthony.instantrecipe.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "User_Settings_Table")
data class UserSettings(@PrimaryKey val key: Int, @ColumnInfo var ingredientSearch: Int) {
}