package com.xyz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StaticResourceConfig implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {


    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:C:/Users/X/Pictures/BSS/");
    }
}
