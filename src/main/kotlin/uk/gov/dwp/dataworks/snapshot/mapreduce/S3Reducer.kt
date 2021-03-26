package uk.gov.dwp.dataworks.snapshot.mapreduce

import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import org.slf4j.LoggerFactory
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class S3Reducer: Reducer<Text, Text, Text, Text>() {

    override fun reduce(key: Text, values: MutableIterable<Text>, context: Context) {
        val target = ByteArrayOutputStream()

        BufferedOutputStream(target).use { output ->
            values.map(Text::getBytes)
                .forEach { value ->
                    output.write(value)
                    output.write(10)
                }
        }

        val request = with(PutObjectRequest.builder()) {
            val key = "map_reduce_output/${String(key.bytes)}"
            bucket("danc-nifi-stub")
            key(key)
            build()
        }

        val output = target.toByteArray()
        val body = RequestBody.fromInputStream(ByteArrayInputStream(output), output.size.toLong())
        s3client.putObject(request, body)
    }

    companion object {
        private val log = LoggerFactory.getLogger(S3Reducer::class.java)
        private val s3client by lazy { S3Client.create() }
    }
}
