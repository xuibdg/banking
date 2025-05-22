package com.core.banking.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<FilterInterceptor> jwtFilter() {
        FilterRegistrationBean<FilterInterceptor> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new FilterInterceptor());
        registrationBean.addUrlPatterns("/api/"); // Validasi semua endpoint
        registrationBean.setName("jwtFilter");
        registrationBean.setOrder(1); // Bisa diatur prioritas filter
        return registrationBean;
    }
}
