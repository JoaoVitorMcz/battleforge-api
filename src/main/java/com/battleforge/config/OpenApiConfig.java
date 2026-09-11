package com.battleforge.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI battleForgeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("BattleForge API")
                        .version("v1")
                        .description("""
                                Turn-based Pokemon battle simulator. All battle logic lives in this API;
                                clients only render the ordered battle events it returns.

                                Fan project, non-commercial and not affiliated with Nintendo,
                                Creatures Inc. or GAME FREAK Inc. Species data comes from PokeAPI.""")
                        .license(new License().name("PokeAPI").url("https://pokeapi.co")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT issued by POST /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
