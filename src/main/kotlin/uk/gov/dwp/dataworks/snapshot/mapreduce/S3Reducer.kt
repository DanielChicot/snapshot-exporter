package uk.gov.dwp.dataworks.snapshot.mapreduce

import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import org.slf4j.LoggerFactory

class S3Reducer: Reducer<Text, Text, Text, Text>() {

    override fun reduce(key: Text, values: MutableIterable<Text>, context: Context) {
        log.info("KEY: '$key'.")
        values.forEach { value: Text ->
            log.info("VALUE: '$value'.")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(S3Reducer::class.java)
    }
}
