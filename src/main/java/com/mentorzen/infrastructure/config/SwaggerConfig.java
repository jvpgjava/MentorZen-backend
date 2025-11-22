package com.mentorzen.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mentor de Redação Zen API")
                        .version("1.0.0")
                        .description("""
                                API REST para o sistema Mentor de Redação Zen - Hackathon Gemini for Education 2024
                                
                                ## Sobre o Projeto
                                O Mentor de Redação Zen é uma solução inovadora que utiliza Inteligência Artificial 
                                para apoiar estudantes do ensino médio no desenvolvimento de suas habilidades de 
                                escrita para o ENEM, com foco especial em saúde mental e bem-estar emocional.
                                
                                ## Funcionalidades Principais
                                - **Gestão de Redações**: CRUD completo para redações dos estudantes
                                - **Análise com IA**: Feedback automático baseado nas 5 competências do ENEM
                                - **Estatísticas**: Acompanhamento do progresso do estudante
                                - **Bem-estar Mental**: Feedback empático e construtivo
                                
                                ## Equipe FloWrite
                                - Eduardo Mello Garcia
                                - Gabriel Oliveira de Matos  
                                - João Gabriel Abreu Baumhardt da Silva
                                - João Vitor Prestes Grando
                                """)
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.mentorzen")
                                .description("Servidor de Produção")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtido através do endpoint de autenticação")));
    }
}
