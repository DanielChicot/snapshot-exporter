package uk.gov.dwp.dataworks.snapshot.htme.service

import uk.gov.dwp.dataworks.snapshot.domain.DataKeyResult
import uk.gov.dwp.dataworks.snapshot.htme.exceptions.DataKeyDecryptionException
import uk.gov.dwp.dataworks.snapshot.htme.exceptions.DataKeyServiceUnavailableException


interface KeyService {

    @Throws(DataKeyServiceUnavailableException::class, DataKeyDecryptionException::class)
    fun decryptKey(encryptionKeyId: String, encryptedKey: String): String

    @Throws(DataKeyServiceUnavailableException::class)
    fun batchDataKey(): DataKeyResult

    fun clearCache()
}
