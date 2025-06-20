package com.core.banking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.config.coa")
@Getter
@Setter
public class AppCoaConfig {


    private String cashTellerId;
    private String feeIncomeId;

}