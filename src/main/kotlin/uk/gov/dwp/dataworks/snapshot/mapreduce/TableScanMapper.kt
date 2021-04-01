package uk.gov.dwp.dataworks.snapshot.mapreduce

import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableMapper
import org.apache.hadoop.io.Text
import uk.gov.dwp.dataworks.logging.DataworksLogger
import uk.gov.dwp.dataworks.snapshot.domain.Record
import uk.gov.dwp.dataworks.snapshot.htme.context.HtmeConfiguration
import uk.gov.dwp.dataworks.snapshot.htme.process.CompoundProcessor
import uk.gov.dwp.dataworks.snapshot.htme.process.impl.HBaseResultProcessor

class TableScanMapper: TableMapper<Text, Text>() {

    override fun map(id: ImmutableBytesWritable, result: Result, context: Context) {
        try {
            HtmeConfiguration.bean(HBaseResultProcessor::class.java).process(result)?.let { sourceRecord ->
//            CompoundProcessor.processor.process(result)?.let { sanitisedRecord ->
                val row = result.row
                val f: Int = if (row[0] < 0) (row[0] + 256) else row[0].toInt()
                val keyBytes = context.configuration["key.bytes"].toInt()
                val key = if (keyBytes < 2) {
                    String.format("db.${context.configuration["source.table"].replace(":", "_")}_%03d", f)
                } else {
                    val g: Int = if (row[1] < 0) (row[1] + 256) else row[1].toInt()
                    String.format("db.${context.configuration["source.table"].replace(":", "_")}_%03d_%03d", f, g)
                }

                val wtf = "${sourceRecord.encryption.encryptedEncryptionKey}|" +
                        "${sourceRecord.encryption.initializationVector}|" +
                        "${sourceRecord.encryption.keyEncryptionKeyId}|" +
                        "${sourceRecord.dbObject}\n"
                val dbObject = Text().apply { set(wtf) }
                context.write(Text().apply { set(key) }, dbObject)
            }
        } catch (e: Exception) {
            log.error("Failed to map row", e)
            e.printStackTrace()
        }
    }

    private fun decryptedRecord(sanitisedRecord: Record) =
        StringBuilder(sanitisedRecord.dbObjectAsString).append('\n').toString()

    companion object {
        private val log = DataworksLogger.getLogger(TableScanMapper::class)
    }
}
