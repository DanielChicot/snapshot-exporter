package uk.gov.dwp.dataworks.snapshot.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "hbase")
class HBaseProperties(var sourceTable: String = "") {

    @Bean
    fun sourceTable(): String = sourceTable
}
