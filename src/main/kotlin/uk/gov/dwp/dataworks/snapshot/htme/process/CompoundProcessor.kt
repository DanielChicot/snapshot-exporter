package uk.gov.dwp.dataworks.snapshot.htme.process

import uk.gov.dwp.dataworks.snapshot.htme.process.impl.HBaseResultProcessor
import org.apache.hadoop.hbase.client.Result

import uk.gov.dwp.dataworks.snapshot.domain.Record
import uk.gov.dwp.dataworks.snapshot.htme.context.HtmeConfiguration.Companion.bean
import uk.gov.dwp.dataworks.snapshot.htme.process.impl.DecryptionProcessor
import uk.gov.dwp.dataworks.snapshot.htme.process.impl.SanitisationProcessor


object CompoundProcessor {
    val processor: Processor<Result, Record> =
        object: Processor<Result, Record> {
           override fun process(item: Result): Record? =
               bean(HBaseResultProcessor::class.java).process(item)
                   ?.let(bean(DecryptionProcessor::class.java)::process)
                   ?.let(bean(SanitisationProcessor::class.java)::process)
        }
}
