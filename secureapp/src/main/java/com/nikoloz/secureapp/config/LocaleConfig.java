package com.nikoloz.secureapp.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * here we set up i18n support for API responses. I am gonna write little explanation of everything down here
 * AcceptHeaderLocaleResolver reads the "Accept-Language" request header to determine the locale, falling back to English if it's missing or unsupported.
 *
 * MessageSource loads from classpath:messages*.properties (UTF-8). Spring will look for messages.properties , messages_en.properties, and messages_ka.properties.
 *
 * LocalValidatorFactoryBean replaces the default validator so that constraint
 * messages using {key} syntax like @NotBlank(message = "{validation.username.notblank}") are resolved through our MessageSource instead of the built-in box/bundle.
 */
@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(List.of(
                Locale.ENGLISH,                // "en"
                Locale.forLanguageTag("ka")    // "ka"
        ));
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source =
                new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        // I researched and it said that 60-second cache is fine for dev, and that in prod the file rarely changes
        source.setCacheSeconds(60);
        source.setUseCodeAsDefaultMessage(false);
        return source;
    }

    /**
     * Wire our MessageSource into the bean-validation engine.
     * Without this {validation.username.notblank} inside @NotBlank it would never reach messages.properties it would just echo the raw key.
     */
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(messageSource);
        return factory;
    }
}
