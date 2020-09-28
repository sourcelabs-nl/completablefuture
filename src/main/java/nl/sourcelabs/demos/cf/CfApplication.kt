package nl.sourcelabs.demos.cf

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class CfApplication {

    @Bean
    fun restTemplateBuilder(): RestTemplateBuilder = RestTemplateBuilder()

    @Bean
    fun restTemplate(): RestTemplate = restTemplateBuilder().rootUri("http://localhost:1080").build()
}

fun main(args: Array<String>) {
    runApplication<CfApplication>(*args)
}

