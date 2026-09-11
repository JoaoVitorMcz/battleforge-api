package com.battleforge;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Real Postgres for integration tests. @ServiceConnection wires the datasource, so no
 * test needs to know the container's generated port or credentials.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainerSupport {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }
}
