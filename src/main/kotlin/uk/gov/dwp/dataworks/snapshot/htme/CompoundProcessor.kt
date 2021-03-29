package uk.gov.dwp.dataworks.snapshot.htme

import uk.gov.dwp.dataworks.snapshot.htme.process.impl.HBaseResultProcessor
import org.apache.hadoop.hbase.client.Result
import uk.gov.dwp.dataworks.snapshot.domain.SourceRecord
import uk.gov.dwp.dataworks.snapshot.htme.process.Processor
import org.springframework.context.annotation.AnnotationConfigApplicationContext

import org.springframework.context.ApplicationContext
import uk.gov.dwp.dataworks.snapshot.domain.DecryptedRecord
import uk.gov.dwp.dataworks.snapshot.htme.context.HtmeConfiguration
import uk.gov.dwp.dataworks.snapshot.htme.process.impl.DecryptionProcessor


object CompoundProcessor {

    fun compoundProcessor(): Processor<Result, DecryptedRecord> {
        val context: ApplicationContext = AnnotationConfigApplicationContext(HtmeConfiguration::class.java)
        val resultProcessor = context.getBean(HBaseResultProcessor::class.java)
        val decryptionProcessor = context.getBean(DecryptionProcessor::class.java)

        return object: Processor<Result, DecryptedRecord> {
            override fun process(item: Result): DecryptedRecord? {
                return resultProcessor.process(item)
                    ?.let(decryptionProcessor::process)
            }
        }
    }
}