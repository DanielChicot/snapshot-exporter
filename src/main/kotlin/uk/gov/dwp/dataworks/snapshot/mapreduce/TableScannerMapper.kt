package uk.gov.dwp.dataworks.snapshot.mapreduce


import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableMapper
import org.apache.hadoop.io.Text
import uk.gov.dwp.dataworks.logging.DataworksLogger
import uk.gov.dwp.dataworks.snapshot.domain.SourceRecord
import uk.gov.dwp.dataworks.snapshot.htme.CompoundProcessor
import uk.gov.dwp.dataworks.snapshot.htme.process.Processor

class TableScannerMapper: TableMapper<Text, Text>() {

    override fun map(id: ImmutableBytesWritable, result: Result, context: Context) {
        processor.process(result)?.let { sourceRecord ->
            val row = result.row
            val f: Int = if (row[0] < 0) (row[0] + 256) else row[0].toInt()
            val key = String.format("db.${context.configuration["source.table"].replace(":", "_")}_%03d", f)
            val dbObject = Text().apply { set(StringBuilder(sourceRecord.dbObject).append('\n').toString()) }
            context.write(Text().apply { set(key) }, dbObject)
        }
    }

    private val processor: Processor<Result, SourceRecord> by lazy {
        CompoundProcessor.compoundProcessor()
    }

    companion object {
        private val log = DataworksLogger.getLogger(TableScannerMapper::class)
    }
}
