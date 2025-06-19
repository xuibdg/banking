package com.core.banking.config;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.codec.Decoder;
import feign.codec.Encoder;


@Configuration
public class FeignClientConfig {

    @Bean
    public Encoder feignEncoder(HttpMessageConverters converters) {
        return new SpringEncoder( () -> converters);
    }

    @Bean
    public Decoder feignDecoder(HttpMessageConverters converters) {
        return new SpringDecoder( () -> converters);
    }

}
