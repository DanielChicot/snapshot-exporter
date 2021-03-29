package uk.gov.dwp.dataworks.snapshot.htme.configuration

import javax.crypto.Cipher

interface CipherInstanceProvider {
    fun cipherInstance(): Cipher
}
