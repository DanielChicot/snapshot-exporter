package uk.gov.dwp.dataworks.snapshot.htme.configuration

import org.apache.http.impl.client.CloseableHttpClient

interface HttpClientProvider {
    fun client(): CloseableHttpClient
}
