package uk.gov.dwp.dataworks.snapshot.htme.process.impl

import io.prometheus.client.Counter
import org.springframework.stereotype.Component
import uk.gov.dwp.dataworks.logging.DataworksLogger
import uk.gov.dwp.dataworks.snapshot.domain.DecryptedRecord
import uk.gov.dwp.dataworks.snapshot.domain.SourceRecord
import uk.gov.dwp.dataworks.snapshot.htme.exceptions.DataKeyServiceUnavailableException
import uk.gov.dwp.dataworks.snapshot.htme.exceptions.DecryptionFailureException
import uk.gov.dwp.dataworks.snapshot.htme.process.Processor
import uk.gov.dwp.dataworks.snapshot.htme.service.CipherService
import uk.gov.dwp.dataworks.snapshot.htme.service.KeyService

@Component
class DecryptionProcessor(private val cipherService: CipherService,
                          private val keyService: KeyService,
                          private val validator: Validator,
                          private val dksNewDataKeyFailuresCounter: Counter) :
    Processor<SourceRecord, DecryptedRecord> {

    @Throws(DataKeyServiceUnavailableException::class)
    override fun process(item: SourceRecord): DecryptedRecord? {
        try {
            val decryptedKey = keyService.decryptKey(
                item.encryption.keyEncryptionKeyId,
                item.encryption.encryptedEncryptionKey)
            val decrypted =
                cipherService.decrypt(
                    decryptedKey,
                    item.encryption.initializationVector,
                    item.dbObject)
            return validator.skipBadDecryptedRecords(item, decrypted)
        } catch (e: DataKeyServiceUnavailableException) {
            dksNewDataKeyFailuresCounter.inc()
            throw e
        } catch (e: Exception) {
           throw DecryptionFailureException(item.hbaseRowId, item.encryption.keyEncryptionKeyId,e)
        }
    }

    companion object {
        val logger = DataworksLogger.getLogger(DecryptionProcessor::class)
    }
}





