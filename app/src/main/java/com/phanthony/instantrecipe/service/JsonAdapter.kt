package com.phanthony.instantrecipe.service

import com.google.gson.GsonBuilder
import com.squareup.moshi.Moshi

interface JsonAdapter {
    fun <T> fromJson(json: String, clazz: Class<T>): T
}

class MoshiJsonAdapter: JsonAdapter {

    val moshi = Moshi.Builder().build()

    override fun <T> fromJson(json: String, clazz: Class<T>): T {
        val adapter = moshi.adapter<T>(clazz)
        return adapter.fromJson(json) as T
    }

}