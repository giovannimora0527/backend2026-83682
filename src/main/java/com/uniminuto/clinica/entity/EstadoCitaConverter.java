package com.uniminuto.clinica.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter
public class EstadoCitaConverter implements AttributeConverter<EstadoCita, String> {
    @Override
    public String convertToDatabaseColumn(EstadoCita estado) {
        return estado == null ? null : estado.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public EstadoCita convertToEntityAttribute(String valor) {
        return valor == null ? null : EstadoCita.valueOf(valor.trim().toUpperCase(Locale.ROOT));
    }
}
