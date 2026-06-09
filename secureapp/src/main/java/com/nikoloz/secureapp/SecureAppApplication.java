package com.nikoloz.secureapp;

import com.nikoloz.secureapp.config.AppSettingsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppSettingsProperties.class)
public class SecureAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureAppApplication.class, args);
    }
}
