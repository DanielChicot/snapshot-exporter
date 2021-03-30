package uk.gov.dwp.dataworks.snapshot.mapreduce

import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import org.slf4j.LoggerFactory
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import uk.gov.dwp.dataworks.snapshot.domain.EncryptingOutputStream
import uk.gov.dwp.dataworks.snapshot.htme.write.RecordWriter.encryptingOutputStream
import java.io.ByteArrayInputStream

class S3Reducer: Reducer<Text, Text, Text, Text>() {

    override fun reduce(key: Text, values: MutableIterable<Text>, context: Context) {
        val encryptingOutputStream = encryptingOutputStream(key(key))
        s3client.putObject(request(context.configuration["s3.bucket"], key),
            requestBody(sourceBytes(encryptingOutputStream, values)))
    }

    private fun requestBody(input: ByteArray): RequestBody =
        RequestBody.fromInputStream(ByteArrayInputStream(input), input.size.toLong())

    private fun sourceBytes(encryptingOutputStream: EncryptingOutputStream, values: MutableIterable<Text>): ByteArray =
        with (encryptingOutputStream) {
            values.map(Text::getBytes).forEach<ByteArray>(this::write)
            close()
            data()
        }

    private fun request(bucket: String, key: Text): PutObjectRequest =
        with (PutObjectRequest.builder()) {
            bucket(bucket)
            key(prefix(key))
            build()
        }

    private fun prefix(key: Text): String = "map_reduce_output/${key(key)}.jsonl"
    private fun key(key: Text) = String(key.bytes.sliceArray(0 until key.length))

    companion object {
        private val log = LoggerFactory.getLogger(S3Reducer::class.java)
        private val s3client by lazy { S3Client.create() }
    }
}
