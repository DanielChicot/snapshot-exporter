package uk.gov.dwp.dataworks.snapshot.htme.configuration

import org.apache.http.client.config.RequestConfig
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import javax.net.ssl.SSLContext


@Component
class SecureHttpClientProvider : HttpClientProvider {

    override fun client(): CloseableHttpClient =
        HttpClients.custom().run {
            setDefaultRequestConfig(requestConfig())
            setSSLSocketFactory(connectionFactory())
            build()
        }


    private fun requestConfig(): RequestConfig =
        RequestConfig.custom().run {
            setConnectTimeout(60_000)
            setConnectionRequestTimeout(60_000)
            setSocketTimeout(60_000)
            build()
        }


    private fun connectionFactory() = SSLConnectionSocketFactory(
        sslContext(),
        arrayOf("TLSv1.2"),
        null,
        SSLConnectionSocketFactory.getDefaultHostnameVerifier())

    private fun sslContext(): SSLContext =
        SSLContexts.custom().run {
            loadKeyMaterial(
                File(identityStore),
                identityStorePassword.toCharArray(),
                identityKeyPassword.toCharArray()) { _, _ -> identityStoreAlias }
            loadTrustMaterial(File(trustStore), trustStorePassword.toCharArray())
            build()
        }

    @Value("\${identity.keystore}")
    private lateinit var identityStore: String

    @Value("\${identity.store.password}")
    private lateinit var identityStorePassword: String

    @Value("\${identity.store.alias}")
    private lateinit var identityStoreAlias: String

    @Value("\${identity.key.password}")
    private lateinit var identityKeyPassword: String

    @Value("\${trust.keystore}")
    private lateinit var trustStore: String

    @Value("\${trust.store.password}")
    private lateinit var trustStorePassword: String
}
