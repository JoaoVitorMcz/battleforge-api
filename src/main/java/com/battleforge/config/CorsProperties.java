package com.battleforge.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Origins allowed to call the API, so the battleforge-web client can be pointed at
 * a different host per environment without a code change.
 */
@ConfigurationProperties(prefix = "battleforge.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
