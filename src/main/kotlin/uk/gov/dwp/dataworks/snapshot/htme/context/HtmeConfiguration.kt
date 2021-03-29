package uk.gov.dwp.dataworks.snapshot.htme.context

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan("uk.gov.dwp.dataworks.snapshot.htme.process", "uk.gov.dwp.dataworks.snapshot.htme.utility")
class HtmeConfiguration {
}
