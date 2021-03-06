package uk.gov.dwp.dataworks.snapshot.htme.process.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.dwp.dataworks.logging.DataworksLogger
import uk.gov.dwp.dataworks.snapshot.domain.EncryptionBlock
import uk.gov.dwp.dataworks.snapshot.domain.SourceRecord
import uk.gov.dwp.dataworks.snapshot.htme.exceptions.MissingFieldException
import uk.gov.dwp.dataworks.snapshot.htme.process.Processor
import uk.gov.dwp.dataworks.snapshot.htme.utility.TextUtils
import java.nio.charset.Charset

@Component
class HBaseResultProcessor(private val textUtils: TextUtils) : Processor<Result, SourceRecord> {

    override fun process(item: Result): SourceRecord? {
        try {
            val idBytes = item.row
            val value = item.value()
            val json = value.toString(Charset.defaultCharset())
            val dataBlock = Gson().fromJson(json, JsonObject::class.java)
            val outerType = dataBlock.getAsJsonPrimitive("@type")?.asString ?: ""
            val messageInfo = dataBlock.getAsJsonObject("message")
            val innerType = messageInfo.getAsJsonPrimitive("@type")?.asString ?: ""
            val encryptedDbObject = messageInfo.getAsJsonPrimitive("dbObject")?.asString
            val encryptionInfo = messageInfo.getAsJsonObject("encryption")
            val encryptedEncryptionKey = encryptionInfo.getAsJsonPrimitive("encryptedEncryptionKey").asString
            val keyEncryptionKeyId = encryptionInfo.getAsJsonPrimitive("keyEncryptionKeyId").asString
            val initializationVector = encryptionInfo.getAsJsonPrimitive("initialisationVector").asString
            val (db, collection) = getDatabaseAndCollection(messageInfo)
            validateMandatoryField(encryptedDbObject, idBytes, "dbObject")
            validateMandatoryField(keyEncryptionKeyId, idBytes, "keyEncryptionKeyId")
            validateMandatoryField(initializationVector, idBytes, "initializationVector")
            validateMandatoryField(encryptedEncryptionKey, idBytes, "encryptedEncryptionKey")
            validateMandatoryField(db, idBytes, "db")
            validateMandatoryField(collection, idBytes, "collection")
            val encryptionBlock = EncryptionBlock(keyEncryptionKeyId, initializationVector, encryptedEncryptionKey)
            return SourceRecord(idBytes, encryptionBlock, encryptedDbObject!!, timestamp(item), db!!, collection!!,
                    if (StringUtils.isNotBlank(outerType)) outerType else "TYPE_NOT_SET",
                    if (StringUtils.isNotBlank(innerType)) innerType else "TYPE_NOT_SET")
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("Error in result processing", e)
            throw e
        }
    }

    private fun timestamp(result: Result): Long =
        result.getColumnLatestCell(columnFamily, columnQualifier).timestamp

    private fun getDatabaseAndCollection(messageInfo: JsonObject): Pair<String?, String?> {
        var db = messageInfo.getAsJsonPrimitive("db")?.asString
        var collection = messageInfo.getAsJsonPrimitive("collection")?.asString

        if (db.isNullOrEmpty() || collection.isNullOrEmpty()) {
            val matcher = textUtils.topicNameTableMatcher(topicName)
            if (db.isNullOrEmpty()) {
                db = matcher?.groupValues?.get(1)
            }
            if (collection.isNullOrEmpty()) {
                collection = matcher?.groupValues?.get(2)
            }
        }

        return Pair(db, collection)
    }


    private fun validateMandatoryField(mandatoryFieldValue: String?, idBytes: ByteArray, fieldName: String) {
        if (mandatoryFieldValue.isNullOrEmpty()) {
            logger.error("Missing field, skipping this record", "id_bytes" to "$idBytes", "field_name" to fieldName)
            throw MissingFieldException(idBytes, fieldName)
        }
    }

    companion object {
        private val logger = DataworksLogger.getLogger(HBaseResultProcessor::class)
        private val columnFamily = Bytes.toBytes("cf")
        private val columnQualifier = Bytes.toBytes("record")
    }

    @Value("\${topic.name}")
    private var topicName: String = ""
}
