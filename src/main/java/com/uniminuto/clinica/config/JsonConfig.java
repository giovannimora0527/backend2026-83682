package com.uniminuto.clinica.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Fecha y hora sin fracciones de segundo (C1). */
@Configuration
public class JsonConfig {

    @Bean
    JsonMapperBuilderCustomizer formatoFechaHora() {
        SimpleModule modulo = new SimpleModule("fecha-hora");
        modulo.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")));
        return builder -> builder.addModule(modulo);
    }
}
