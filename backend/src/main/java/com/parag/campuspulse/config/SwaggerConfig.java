package com.parag.campuspulse.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Swagger UI at /swagger-ui/index.html
 *
 * Key feature: adds the "Authorize" button so you can paste your JWT token
 * and have it automatically sent as "Authorization: Bearer <token>" on all
 * requests you fire from the UI.
 *
 * Usage:
 *  1. POST /api/auth/login → copy the token from the response
 *  2. Click "Authorize" at the top of Swagger UI
 *  3. Paste:  Bearer <your-token>
 *  4. All subsequent requests in the UI will include that header
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI campusPulseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CampusPulse API")
                        .description("""
                                Campus Pulse — RBAC Event Management System for SMVDU.
                                
                                **Roles:** STUDENT · EVENT_COORDINATOR · FACULTY_AUTHORITY · SYSTEM_ADMIN
                                
                                **Auth flow:**
                                1. `POST /api/auth/change-password` — set your password (first login)
                                2. `POST /api/auth/login` — get a JWT token
                                3. Click **Authorize** above → paste `Bearer <token>`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Parag")
                                .email("admin@smvdu.ac.in")))

                // Register the Bearer token scheme
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token here (without the 'Bearer ' prefix)")));
    }
}