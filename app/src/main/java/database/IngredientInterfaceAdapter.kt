package database

import com.google.gson.*
import java.lang.reflect.Type

class IngredientInterfaceAdapter : JsonSerializer<Any>, JsonDeserializer<Any> {

    companion object {
        const val CLASSNAME = "CLASSNAME"
        const val DATA = "DATA"
    }

    override fun serialize(src: Any?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty(CLASSNAME, src!!.javaClass.name)
        jsonObject.add(DATA, context!!.serialize(src))
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Any {
        val jsonObject = json!!.asJsonObject
        val prim = jsonObject.get(CLASSNAME)
        val className = prim.asString
        val objectClass = getObjectClass(className)
        return context!!.deserialize(jsonObject.get(DATA), objectClass)
    }

    private fun getObjectClass(className: String): Class<*> {
        try {
            return Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw JsonParseException(e.message)
        }

    }
}