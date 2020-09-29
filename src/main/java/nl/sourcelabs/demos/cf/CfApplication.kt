package nl.sourcelabs.demos.cf

import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class CfApplication {

    @Bean
    fun myHttpClient(): CloseableHttpClient {
        val httpClientBuilder: HttpClientBuilder = HttpClientBuilder.create()
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectTimeout(2000)
                    .setSocketTimeout(2000)
                    .build())
            .setMaxConnTotal(100)
            .setMaxConnPerRoute(100)
        return httpClientBuilder.build()
    }

    @Bean
    fun myTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .requestFactory { HttpComponentsClientHttpRequestFactory(myHttpClient()) }
            .rootUri("http://localhost:1080")
            .build()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(CfApplication::class.java, *args)
}