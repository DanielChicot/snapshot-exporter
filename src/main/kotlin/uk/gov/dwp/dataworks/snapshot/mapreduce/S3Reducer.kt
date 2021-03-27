package uk.gov.dwp.dataworks.snapshot.mapreduce

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
        s3client.putObject(request(key), requestBody(sourceBytes(values)))
    }

    private fun requestBody(input: ByteArray): RequestBody =
            RequestBody.fromInputStream(ByteArrayInputStream(input), input.size.toLong())

    private fun sourceBytes(values: MutableIterable<Text>): ByteArray =
            ByteArrayOutputStream().run {
                BufferedOutputStream(this).use { output ->
                    values.map(Text::getBytes)
                        .forEach { value ->
                            output.write(value)
                            output.write(10)
                        }
                }
                toByteArray()
            }

    private fun request(key: Text): PutObjectRequest =
        with(PutObjectRequest.builder()) {
            val prefix = "map_reduce_output/${key(key)}.jsonl"
            bucket("danc-nifi-stub")
            key(prefix)
            build()
        }

    private fun key(key: Text) = String(key.bytes.sliceArray(0 until key.length))

    companion object {
        private val log = LoggerFactory.getLogger(S3Reducer::class.java)
        private val s3client by lazy { S3Client.create() }
    }
}
