package uk.gow.dwp.dataworks.snapshot

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.util.Tool
import org.apache.hadoop.util.ToolRunner
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration::class])
class SnapshotExporterApplication(private val exporter: Tool): CommandLineRunner {

	override fun run(vararg args: String) {
		ToolRunner.run(HBaseConfiguration.create(), exporter, args)
	}

	companion object {
		private val logger = LoggerFactory.getLogger(SnapshotExporterApplication::class.java)
	}
}

fun main(args: Array<String>) {
	runApplication<SnapshotExporterApplication>(*args)
}
