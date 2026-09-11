package com.gym.gym_checkin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "https://thetroop.in",
                                "https://www.thetroop.in",
                                "https://gym-checkin-frontend.onrender.com"
                        )
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }
}