package com.mentorzen.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.profile-pictures.path:uploads/profile-pictures}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = Paths.get(uploadPath).toAbsolutePath().toString();

        registry.addResourceHandler("/uploads/profile-pictures/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600);
    }
}
