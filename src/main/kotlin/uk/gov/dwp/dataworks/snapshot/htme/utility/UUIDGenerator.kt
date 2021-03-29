package uk.gov.dwp.dataworks.snapshot.htme.utility

import org.springframework.stereotype.Component
import java.util.*

@Component
class UUIDGenerator {

    fun randomUUID(): String {
        return UUID.randomUUID().toString()
    }
}
