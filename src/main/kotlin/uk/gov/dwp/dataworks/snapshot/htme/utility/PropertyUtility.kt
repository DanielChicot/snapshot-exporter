package uk.gov.dwp.dataworks.snapshot.htme.utility

object PropertyUtility {
    fun correlationId(): String =
        System.getProperty("correlation_id") ?: System.getenv("CORRELATION_ID") ?: throw Exception("No correlation id specified.")
}
