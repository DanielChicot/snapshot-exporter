package uk.gov.dwp.dataworks.snapshot.mapreduce

import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer
import org.slf4j.LoggerFactory
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import uk.gov.dwp.dataworks.snapshot.domain.EncryptingOutputStream
import uk.gov.dwp.dataworks.snapshot.domain.EncryptionBlock
import uk.gov.dwp.dataworks.snapshot.htme.context.HtmeConfiguration.Companion.bean
import uk.gov.dwp.dataworks.snapshot.htme.service.impl.AESCipherService
import uk.gov.dwp.dataworks.snapshot.htme.service.impl.HttpKeyService
import uk.gov.dwp.dataworks.snapshot.htme.write.RecordWriter.encryptingOutputStream
import java.io.ByteArrayInputStream

class S3Reducer: Reducer<Text, Text, Text, Text>() {

    override fun reduce(key: Text, values: MutableIterable<Text>, context: Context) {

//        val wtf = "${sourceRecord.encryption.encryptedEncryptionKey}|" +
//                "${sourceRecord.encryption.initializationVector}|" +
//                "${sourceRecord.encryption.keyEncryptionKeyId}|" +
//                "${sourceRecord.dbObject}\n"

        val list = values.map(this@S3Reducer::validBytes).map {
            val (encryptedEncryptionKey, initialisationVector, keyEncryptionKeyId, dbObject) = it.split("|")
            Pair(EncryptionBlock(keyEncryptionKeyId, initialisationVector, encryptedEncryptionKey), dbObject)
        }

        val dks = bean(HttpKeyService::class.java)


        val datakeys: Map<String, String> = list.map { (encryptionBlock, _) ->
            Pair(encryptionBlock.encryptedEncryptionKey, encryptionBlock.keyEncryptionKeyId)
        }.toSet().mapNotNull { (encryptedEncryptionKey, keyEncryptionKeyId) ->
            try {
                val decrypted = dks.decryptKey(keyEncryptionKeyId, encryptedEncryptionKey)
                Pair(encryptedEncryptionKey, decrypted)
            } catch (e: Exception) {
                log.error("Failed to decrypt key", e, "key" to encryptedEncryptionKey, "key_id" to keyEncryptionKeyId)
                e.printStackTrace(System.err)
                null
            }
        }.associate { (encryptedEncryptionKey, decrypted) ->
            encryptedEncryptionKey to decrypted
        }

        val cipherService = bean(AESCipherService::class.java)

        val dbObjects: List<String> = list.asSequence().map { (encryptionBlock, dbObject) ->
            Triple(dbObject, datakeys[encryptionBlock.encryptedEncryptionKey], encryptionBlock.initializationVector)
        }.mapNotNull { (dbObject, decryptedEncryptionKey: String?, initialisationVector) ->
            if (decryptedEncryptionKey == null) {
                log.error("NO KEY FOUND!!!!!!")
            }
            decryptedEncryptionKey?.let {
                try {
                    cipherService.decrypt(it, initialisationVector, dbObject)
                } catch (e: Exception) {
                    println("Failed to decrypt: '$dbObject'")
                    e.printStackTrace()
                    null
                }
            }
        }.toList()

        log.info("No of values: ${dbObjects.size}")
        log.info("No of keys: ${datakeys.size}")

        val encryptingOutputStream = encryptingOutputStream(validBytes(key))

        s3client.putObject(request(context.configuration["s3.bucket"], key),
            requestBody(sourceBytes(encryptingOutputStream, dbObjects)))
    }

    private fun requestBody(input: ByteArray): RequestBody =
        RequestBody.fromInputStream(ByteArrayInputStream(input), input.size.toLong())

    private fun sourceBytes(encryptingOutputStream: EncryptingOutputStream,
                            dbObjects: List<String>): ByteArray =
        with (encryptingOutputStream) {
            dbObjects.map(String::toByteArray).forEach(this::write)
            close()
            data()
        }

    private fun request(bucket: String, key: Text): PutObjectRequest =
        with (PutObjectRequest.builder()) {
            bucket(bucket)
            key(prefix(key))
            build()
        }

    private fun prefix(key: Text): String = "map_reduce_output/${validBytes(key)}.txt.gz.enc"
    private fun validBytes(key: Text) = String(key.bytes.sliceArray(0 until key.length))

    companion object {
        private val log = LoggerFactory.getLogger(S3Reducer::class.java)
        private val s3client by lazy { S3Client.create() }
    }
}
