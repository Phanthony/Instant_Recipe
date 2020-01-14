package com.phanthony.instantrecipe.database

data class Ingredient(val aisle: String,
                 val amount: Float,
                 val id: Int, val name: String,
                 val original: String,
                 val originalName: String,
                 val originalString: String)