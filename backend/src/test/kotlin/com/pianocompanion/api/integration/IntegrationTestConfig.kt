package com.pianocompanion.api.integration

import com.pianocompanion.api.global.config.CorsProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class IntegrationTestConfig {

    @Bean
    fun corsProperties(): CorsProperties = CorsProperties(allowedOrigins = "http://localhost:3000")
}
