package uk.gov.dwp.dataworks.snapshot.tool

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.conf.Configured
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat
import org.apache.hadoop.util.Tool
import org.slf4j.LoggerFactory
import uk.gov.dwp.dataworks.snapshot.mapreduce.S3Reducer
import uk.gov.dwp.dataworks.snapshot.mapreduce.TableScanMapper
import java.text.SimpleDateFormat
import java.util.*


class Exporter: Configured(), Tool {

    override fun run(args: Array<out String>): Int =
            initialisedTableMapperJob(args[0], args[1], args[2].toInt()).run {
                if (waitForCompletion(true)) {
                    0
                } else {
                    log.error("Job failed")
                    1
                }
            }

    private fun initialisedTableMapperJob(sourceTable: String, targetBucket: String, reducerCount: Int): Job =
            job(sourceTable, targetBucket, reducerCount).also { job ->
                TableMapReduceUtil.initTableMapperJob(sourceTable,
                    scan(),
                    TableScanMapper::class.java,
                    Text::class.java,
                    Text::class.java,
                    job)
            }

    private fun scan(): Scan =
            Scan().apply {
                caching = 500
                cacheBlocks = false
            }

    private fun job(sourceTable: String, targetBucket: String, reducerCount: Int): Job =
            Job.getInstance(configuration(sourceTable, targetBucket), jobName(sourceTable)).apply {
                setJarByClass(TableScanMapper::class.java)
                outputFormatClass = NullOutputFormat::class.java
                reducerClass = S3Reducer::class.java
                numReduceTasks = reducerCount
            }

    private fun configuration(sourceTable: String, targetBucket: String): Configuration =
            HBaseConfiguration.create().apply {
                this["source.table"] = sourceTable
                this["s3.bucket"] = targetBucket
            }

    private fun jobName(sourceTable: String) = "Snapshot exporter '$sourceTable': " +
            "'${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}'"

    companion object {
        private val log = LoggerFactory.getLogger(Exporter::class.java)
    }
}
