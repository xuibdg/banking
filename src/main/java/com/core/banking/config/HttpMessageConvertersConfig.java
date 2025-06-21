package com.core.banking.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class HttpMessageConvertersConfig {

    @Bean
    public HttpMessageConverters messageConverters() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                JsonToken token = p.getCurrentToken();
                if (token == JsonToken.VALUE_STRING || token == JsonToken.VALUE_NUMBER_INT) {
                    return p.getText();
                }
                return null;
            }
        });
        objectMapper.registerModule(module);

        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(objectMapper);

        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(jsonConverter);
        converters.add(stringConverter);
        converters.add(new TextPlainJackson2HttpMessageConverter(objectMapper));

        return new HttpMessageConverters(true, converters);
    }
}
