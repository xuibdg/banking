package com.core.banking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import java.io.IOException;

public class TextPlainJackson2HttpMessageConverter extends AbstractHttpMessageConverter<Object>{

    private final ObjectMapper objectMapper;

    public TextPlainJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(MediaType.TEXT_PLAIN);
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
        throws IOException {
        return objectMapper.readValue(inputMessage.getBody(), clazz);
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage)
        throws IOException {
        objectMapper.writeValue(outputMessage.getBody(), o);
    }
}
