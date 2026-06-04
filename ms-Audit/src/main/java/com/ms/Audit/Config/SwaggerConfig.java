package com.ms.Audit.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        Contact contacto = new Contact()
                .name("Juan Fernández & Víctor Huenchucheo")
                .email("Ju.fernandeza@duocuc.cl")
                .url("https://github.com/ricks86/ProyectoFS1ForoDuocRV");

        return new OpenAPI()
                .info(new Info()
                        .title("API de microservicio Adit")
                        .version("1.0")
                        .description("Microservicio interno, encargado del registro de todas las acciones criticas de demás microservicios")
                        .contact(contacto));
    }
}
