package uk.gov.dwp.dataworks.snapshot.htme.context

import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import uk.gov.dwp.dataworks.logging.DataworksLogger
import uk.gov.dwp.dataworks.snapshot.htme.configuration.CipherInstanceProvider
import uk.gov.dwp.dataworks.snapshot.htme.configuration.CompressionInstanceProvider
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher

@Configuration
@ComponentScan(
    "uk.gov.dwp.dataworks.snapshot.htme.configuration",
    "uk.gov.dwp.dataworks.snapshot.htme.process",
    "uk.gov.dwp.dataworks.snapshot.htme.service",
    "uk.gov.dwp.dataworks.snapshot.htme.utility")
@PropertySources(PropertySource("classpath:application.properties"),
    PropertySource("file:/opt/emr/dks.properties"))
class HtmeConfiguration {

    @Bean
    fun gzCompressor() = object: CompressionInstanceProvider {
        override fun compressorOutputStream(outputStream: OutputStream) =
            CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.GZIP, outputStream)

        override fun compressionExtension() = "gz"
    }


    @Bean
    fun cipherInstanceProvider(): CipherInstanceProvider {
        return object: CipherInstanceProvider {
            override fun cipherInstance(): Cipher {
                return Cipher.getInstance("AES/CTR/NoPadding", "BC")
            }
        }
    }

    @Bean
    fun weakRandom() = SecureRandom.getInstance("SHA1PRNG")!!

    @Bean
    fun manifestOutputDirectory() = manifestOutputDirectory


    @Value("\${manifest.output.directory:/tmp}")
    private lateinit var manifestOutputDirectory: String

    companion object {
        val logger = DataworksLogger.getLogger(HtmeConfiguration::class)
        fun <T> bean(classOfT: Class<T>): T = applicationContext.getBean(classOfT)
        fun <T> bean(name: String, classOfT: Class<T>): T = applicationContext.getBean(name, classOfT)

        private val applicationContext: ApplicationContext by lazy {
            AnnotationConfigApplicationContext(HtmeConfiguration::class.java)
        }
    }
}
