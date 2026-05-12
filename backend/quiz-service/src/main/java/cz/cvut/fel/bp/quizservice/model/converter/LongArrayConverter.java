package cz.cvut.fel.bp.quizservice.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.stream.Collectors;

@Converter
public class LongArrayConverter implements AttributeConverter<Long[], String> {

    @Override
    public String convertToDatabaseColumn(Long[] attribute) {
        if (attribute == null || attribute.length == 0) return "";
        return Arrays.stream(attribute)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public Long[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return new Long[0];
        return Arrays.stream(dbData.split(","))
                .map(Long::valueOf)
                .toArray(Long[]::new);
    }
}

