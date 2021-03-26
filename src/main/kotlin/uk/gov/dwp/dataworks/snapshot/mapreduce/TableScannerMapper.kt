package uk.gov.dwp.dataworks.snapshot.mapreduce

import org.apache.hadoop.hbase.mapreduce.TableMapper
import org.apache.hadoop.io.Text


import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class TableScannerMapper: TableMapper<Text, Text>() {

    override fun map(row: ImmutableBytesWritable, value: org.apache.hadoop.hbase.client.Result, context: Context) {
        log.info("ROW: '$row'.")
        log.info("VALUE: '$value'.")
        log.info("CONTEXT: '$context'.")
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TableScannerMapper::class.java)
    }
}
