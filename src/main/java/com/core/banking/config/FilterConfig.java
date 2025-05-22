package com.core.banking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterInterceptor filterInterceptor() {
        return new FilterInterceptor();
    }
}
