package uk.gov.dwp.dataworks.snapshot

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.util.ToolRunner
import uk.gov.dwp.dataworks.snapshot.tool.Exporter

fun main(args: Array<String>) {
    ToolRunner.run(HBaseConfiguration.create(), Exporter(), args)
}
