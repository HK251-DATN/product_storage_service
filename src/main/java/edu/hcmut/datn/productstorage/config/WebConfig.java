package edu.hcmut.datn.productstorage.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Allow your frontend origin
        config.addAllowedOrigin("http://10.205.183.122:5173");
        config.addAllowedOrigin("http://10.185.89.85:5173");// Vite default port
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://10.194.144.9:3000");// Alternative port

        // Allow all headers
        config.addAllowedHeader("*");

        // Allow all HTTP methods
        config.addAllowedMethod("*");

        // How long the response from a pre-flight request can be cached
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}

