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
import uk.gov.dwp.dataworks.snapshot.mapreduce.TableScannerMapper
import java.text.SimpleDateFormat
import java.util.*


class Exporter: Configured(), Tool {

    override fun run(args: Array<out String>): Int =
            initialisedTableMapperJob(args[0]).run {
                if (waitForCompletion(true)) {
                    0
                } else {
                    log.error("Job failed")
                    1
                }
            }

    private fun initialisedTableMapperJob(sourceTable: String): Job =
            job(sourceTable).also { job ->
                TableMapReduceUtil.initTableMapperJob(sourceTable,
                    scan(),
                    TableScannerMapper::class.java,
                    Text::class.java,
                    Text::class.java,
                    job)
            }

    private fun scan(): Scan =
            Scan().apply {
                caching = 500
                cacheBlocks = false
            }

    private fun job(sourceTable: String): Job =
            Job.getInstance(configuration(sourceTable), jobName(sourceTable)).apply {
                setJarByClass(TableScannerMapper::class.java)
                outputFormatClass = NullOutputFormat::class.java
                reducerClass = S3Reducer::class.java
                numReduceTasks = 1
            }

    private fun configuration(sourceTable: String): Configuration =
            HBaseConfiguration.create().apply { this["source.table"] = sourceTable }

    private fun jobName(sourceTable: String) = "Snapshot exporter '$sourceTable': " +
            "'${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}'"

    companion object {
        private val log = LoggerFactory.getLogger(Exporter::class.java)
    }
}
