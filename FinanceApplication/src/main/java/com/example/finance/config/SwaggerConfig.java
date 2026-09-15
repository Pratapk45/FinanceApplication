package com.example.finance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI financeOpenAPI() {

        return new OpenAPI()
                .components(new Components())
                .info(
                        new Info()
                                .title("Finance Management System API")
                                .description(
                                        "Production-style REST APIs for managing " +
                                        "customers, accounts, transactions, loans " +
                                        "and investments."
                                )
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("Finance Application Team")
                                                .email("support@finance.com")
                                )
                                .license(
                                        new License()
                                                .name("Internal Finance Application")
                                )
                );
    }
}