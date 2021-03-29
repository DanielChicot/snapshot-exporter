package uk.gov.dwp.dataworks.snapshot.htme.utility

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.dwp.dataworks.snapshot.htme.exceptions.BlockedTopicException

@Component
class FilterBlockedTopicsUtils {

    @Value("\${blocked.topics:NOT_SET}")
    private var blockedTopics: String = "NOT_SET"

    @Throws(BlockedTopicException::class)
    fun isTopicBlocked(topic: String) {
        val blockedTopicsList: MutableList<String> = mutableListOf()
        
        if (blockedTopics.contains(',')) {
            blockedTopicsList.addAll(blockedTopics.split(","))
        } else {
            blockedTopicsList.add(blockedTopics)
        }

        if (blockedTopicsList.contains(topic))
            throw BlockedTopicException(topic)
    }
}
