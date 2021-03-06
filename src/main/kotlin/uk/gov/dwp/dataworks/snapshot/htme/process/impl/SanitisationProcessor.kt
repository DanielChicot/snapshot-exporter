package uk.gov.dwp.dataworks.snapshot.htme.process.impl

import org.springframework.stereotype.Component
import uk.gov.dwp.dataworks.logging.DataworksLogger
import uk.gov.dwp.dataworks.snapshot.domain.DecryptedRecord
import uk.gov.dwp.dataworks.snapshot.domain.Record
import uk.gov.dwp.dataworks.snapshot.htme.exceptions.DataKeyServiceUnavailableException
import uk.gov.dwp.dataworks.snapshot.htme.process.Processor

// See https://projects.ucd.gpn.gov.uk/browse/DW-2374
@Component
class SanitisationProcessor : Processor<DecryptedRecord, Record> {

    val replacementRegex = """(?<!\\)\\[r|n]""".toRegex()

    @Throws(DataKeyServiceUnavailableException::class)
    override fun process(item: DecryptedRecord): Record? {
        try {
            val output = sanitiseCollectionSpecific(item)
            val replacedOutput = output.replace("$", "d_")
                .replace("\\u0000", "")
                .replace("_archivedDateTime", "_removedDateTime")
                .replace("_archived", "_removed")

            val manifestRecord = item.manifestRecord
            return Record(replacedOutput, manifestRecord)
        } catch (e: Exception) {
            logger.error("Error sanitising", e)
            throw e
        }
    }

    fun sanitiseCollectionSpecific(input: DecryptedRecord): String {
        val db = input.manifestRecord.db
        val collection = input.manifestRecord.collection
        val dbObject = input.dbObject
        if ((db == "penalties-and-deductions" && collection == "sanction")
            || (db == "core" && collection == "healthAndDisabilityDeclaration")
            || (db == "accepted-data" && collection == "healthAndDisabilityCircumstances")) {
            logger.debug("Sanitising output", "db_name" to db, "collection_name" to collection)
            return dbObject.toString().replace(replacementRegex, "")
        }
        return dbObject.toString()
    }

    companion object {
        val logger = DataworksLogger.getLogger(SanitisationProcessor::class)
    }
}
