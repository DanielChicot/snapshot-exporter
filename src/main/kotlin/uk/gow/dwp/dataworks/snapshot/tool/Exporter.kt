package uk.gow.dwp.dataworks.snapshot.tool

import org.apache.hadoop.conf.Configured
import org.apache.hadoop.util.Tool
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Exporter(private val sourceTable: String): Configured(), Tool {

    override fun run(args: Array<out String>): Int {
        conf.also { configuration ->
            log.info("SOURCE TABLE: $sourceTable")
            configuration["hbase.table"] = sourceTable
            println("ARGS: '${args.asList()}'.")
        }

        return 0
    }

    companion object {
        private val log = LoggerFactory.getLogger(Exporter::class.java)
    }
}
