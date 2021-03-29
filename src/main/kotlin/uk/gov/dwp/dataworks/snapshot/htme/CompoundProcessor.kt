package uk.gov.dwp.dataworks.snapshot.htme

import uk.gov.dwp.dataworks.snapshot.htme.process.impl.HBaseResultProcessor
import org.apache.hadoop.hbase.client.Result
import uk.gov.dwp.dataworks.snapshot.domain.SourceRecord
import uk.gov.dwp.dataworks.snapshot.htme.process.Processor
import org.springframework.context.annotation.AnnotationConfigApplicationContext

import org.springframework.context.ApplicationContext
import uk.gov.dwp.dataworks.snapshot.htme.context.HtmeConfiguration


object CompoundProcessor {

    fun compoundProcessor(): Processor<Result, SourceRecord> {

        val context: ApplicationContext = AnnotationConfigApplicationContext(HtmeConfiguration::class.java)
        val resultProcessor = context.getBean(HBaseResultProcessor::class.java)

        return object: Processor<Result, SourceRecord> {
            override fun process(item: Result): SourceRecord? {
                return resultProcessor.process(item)
            }
        }
    }
}
