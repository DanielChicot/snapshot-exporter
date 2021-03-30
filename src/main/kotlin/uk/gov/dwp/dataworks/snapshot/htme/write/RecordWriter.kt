package uk.gov.dwp.dataworks.snapshot.htme.write

import uk.gov.dwp.dataworks.snapshot.domain.EncryptingOutputStream
import uk.gov.dwp.dataworks.snapshot.htme.configuration.CompressionInstanceProvider
import uk.gov.dwp.dataworks.snapshot.htme.context.HtmeConfiguration
import uk.gov.dwp.dataworks.snapshot.htme.context.HtmeConfiguration.Companion.bean
import uk.gov.dwp.dataworks.snapshot.htme.exceptions.DataKeyServiceUnavailableException
import uk.gov.dwp.dataworks.snapshot.htme.service.impl.AESCipherService
import uk.gov.dwp.dataworks.snapshot.htme.service.impl.HttpKeyService
import java.io.*
import java.security.Key
import java.security.SecureRandom
import java.util.*
import javax.crypto.spec.SecretKeySpec

object RecordWriter {

    fun encryptingOutputStream(prefix: String): EncryptingOutputStream {
        try {
            val keyResponse = keyService.batchDataKey()
            val key: Key = SecretKeySpec(Base64.getDecoder().decode(keyResponse.plaintextDataKey), "AES")
            val byteArrayOutputStream = ByteArrayOutputStream()
            val initialisationVector = ByteArray(16).apply {
                secureRandom.nextBytes(this)
            }
            val cipherOutputStream = cipherService.cipherOutputStream(key, initialisationVector, byteArrayOutputStream)
            val compressingStream = compressionInstanceProvider.compressorOutputStream(cipherOutputStream)
            val manifestFile = File("$manifestOutputDirectory/$prefix.csv")
            val manifestWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(manifestFile)))

            return EncryptingOutputStream(BufferedOutputStream(compressingStream),
                byteArrayOutputStream,
                keyResponse,
                Base64.getEncoder().encodeToString(initialisationVector),
                manifestFile,
                manifestWriter)
        } catch (e: DataKeyServiceUnavailableException) {
//            dksNewDataKeyFailuresCounter.inc()
            throw e
        }
    }


    private val manifestOutputDirectory by lazy {
        bean("manifestOutputDirectory", String::class.java)
    }

    private val keyService by lazy {
        bean(HttpKeyService::class.java)
    }

    private val secureRandom by lazy {
        bean(SecureRandom::class.java)
    }

    private val cipherService by lazy {
        bean(AESCipherService::class.java)
    }

    private val compressionInstanceProvider by lazy {
        bean(CompressionInstanceProvider::class.java)
    }

}
