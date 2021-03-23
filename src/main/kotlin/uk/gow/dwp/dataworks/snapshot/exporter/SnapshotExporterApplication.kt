package uk.gow.dwp.dataworks.snapshot.exporter

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnapshotExporterApplication: CommandLineRunner {
	override fun run(vararg args: String) {
		logger.info("WOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")
	}

	companion object {
		private val logger = LoggerFactory.getLogger(SnapshotExporterApplication::class.java)
	}
}

fun main(args: Array<String>) {
	runApplication<SnapshotExporterApplication>(*args)
}
