package uk.gov.dwp.dataworks.snapshot.htme.utility

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser

open class JsonUtils {
    fun sortJsonByKey(unsortedJson: String): String {
        val parser: Parser = Parser.default()
        val stringBuilder = StringBuilder(unsortedJson)
        val jsonObject = parser.parse(stringBuilder) as JsonObject
        val sortedEntries = jsonObject.toSortedMap(compareBy { it })
        val jsonSorted = JsonObject(sortedEntries)
        return jsonSorted.toJsonString()
    }
}
