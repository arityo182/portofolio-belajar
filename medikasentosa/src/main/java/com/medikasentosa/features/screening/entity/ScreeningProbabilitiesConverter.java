package com.medikasentosa.features.screening.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

/**
 * Menyimpan distribusi probabilitas 3 kelas dari model ML sebagai JSON di
 * kolom TEXT. Converter dipakai agar entity tetap bertipe {@code Map} tanpa
 * bergantung pada tipe kolom JSONB yang tidak selalu tersedia.
 */
@Converter
public class ScreeningProbabilitiesConverter
        implements AttributeConverter<Map<String, Double>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Double>> TYPE = new TypeReference<>() { };

    @Override
    public String convertToDatabaseColumn(Map<String, Double> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Gagal menyimpan probabilitas screening", e);
        }
    }

    @Override
    public Map<String, Double> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(dbData, TYPE);
        } catch (Exception e) {
            return null;
        }
    }
}
