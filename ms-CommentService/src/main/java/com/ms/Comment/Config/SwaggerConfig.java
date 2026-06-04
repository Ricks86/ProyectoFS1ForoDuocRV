package com.ms.Comment.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        Contact contacto = new Contact()
                .name("Juan Fernández & Víctor Huenchucheo")
                .email("Ju.fernandeza@duocuc.cl")
                .url("https://github.com/ricks86/ProyectoFS1ForoDuocRV");

        return new OpenAPI()
                .info(new Info()
                        .title("API del Microservicio Comment")
                        .version("1.0")
                        .description("Documentación interactiva de los endpoints.")
                        .contact(contacto))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
