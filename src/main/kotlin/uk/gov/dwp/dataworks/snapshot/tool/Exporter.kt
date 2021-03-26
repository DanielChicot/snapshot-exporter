package uk.gov.dwp.dataworks.snapshot.tool

import org.apache.hadoop.conf.Configured
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat
import org.apache.hadoop.util.Tool
import org.slf4j.LoggerFactory
import uk.gov.dwp.dataworks.snapshot.mapreduce.TableScannerMapper
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class Exporter: Configured(), Tool {

    override fun run(args: Array<out String>): Int {
        val configuration = HBaseConfiguration.create()
        println("ARGS: '${args.asList()}'.")
        val sourceTable = "core:statement"
        val job = Job.getInstance(configuration,"Snapshot exporter '$sourceTable': " +
                    "'${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}'").apply {
            setJarByClass(TableScannerMapper::class.java)
            outputFormatClass = NullOutputFormat::class.java
        }
        val scan = Scan().apply {
            caching = 500
            cacheBlocks = false
        }

        TableMapReduceUtil.initTableMapperJob(sourceTable, scan, TableScannerMapper::class.java, null, null, job)

        val b = job.waitForCompletion(true)
        if (!b) {
            throw IOException("error with job!")
        }

        return 0
    }

//    private fun jobInstance(configuration: Configuration) =
//        Job.getInstance(configuration,
//            "Snapshot exporter '$sourceTable': " +
//                    "'${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}'").apply {
//            setJarByClass(TableScannerMapper::class.java)
//            mapperClass = TableScannerMapper::class.java
//            mapOutputKeyClass = ImmutableBytesWritable::class.java
//            mapOutputValueClass = KeyValue::class.java
//            inputFormatClass = UcInputFormat::class.java
//        }

    companion object {
        private val log = LoggerFactory.getLogger(Exporter::class.java)
    }
}
