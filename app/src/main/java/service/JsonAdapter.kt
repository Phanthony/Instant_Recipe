package service

import com.google.gson.GsonBuilder

interface JsonAdapter {
    fun <T> fromJson(json: String, clazz: Class<T>): T
}

class GsonJsonAdapter(): JsonAdapter{

    val gson =  GsonBuilder().create()

    override fun <T> fromJson(json: String, clazz: Class<T>): T {
        val adapter = gson.getAdapter(clazz)
        return adapter.fromJson(json) as T
    }

}