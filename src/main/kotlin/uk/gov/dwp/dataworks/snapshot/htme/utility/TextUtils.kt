package uk.gov.dwp.dataworks.snapshot.htme.utility

import org.springframework.stereotype.Component

@Component
class TextUtils {
    fun topicNameTableMatcher(topicName: String) = qualifiedTablePattern.find(topicName)
    private val qualifiedTablePattern = Regex("""^(?:\w+\.)?([-\w]+)\.([-\w]+)$""")
}
