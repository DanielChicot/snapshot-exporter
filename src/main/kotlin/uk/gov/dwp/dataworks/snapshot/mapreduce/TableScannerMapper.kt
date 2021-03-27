package uk.gov.dwp.dataworks.snapshot.mapreduce

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.apache.hadoop.hbase.mapreduce.TableMapper
import org.apache.hadoop.io.Text


import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.charset.Charset


class TableScannerMapper: TableMapper<Text, Text>() {

    override fun map(id: ImmutableBytesWritable, result: org.apache.hadoop.hbase.client.Result, context: Context) {
        val row = result.row
        val value = result.value()
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

        log.info("outerType: '$outerType'.")
        log.info("innerType: '$innerType'.")
        log.info("encryptedDbObject: '$encryptedDbObject'.")
        log.info("encryptedEncryptionKey: '$encryptedEncryptionKey'.")
        log.info("keyEncryptionKeyId: '$keyEncryptionKeyId'.")
        log.info("initializationVector: '$initializationVector'.")
        val f: Int = if (row[0] < 0) (row[0] + 256) else row[0].toInt()
        val key = String.format("db.${context.configuration["source.table"].replace(":", "_")}_%03d", f)
        log.info("f: '$f'.")
        log.info("key: '$key'.")
        val dbObject = Text().apply { set(encryptedDbObject) }
        context.write(Text().apply { set(key) }, dbObject)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TableScannerMapper::class.java)
    }
}
