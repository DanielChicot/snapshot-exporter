package uk.gov.dwp.dataworks.snapshot.htme.configuration

import org.apache.commons.compress.compressors.CompressorOutputStream
import java.io.OutputStream

interface CompressionInstanceProvider {
    fun compressorOutputStream(outputStream: OutputStream): CompressorOutputStream
    fun compressionExtension(): String
}
