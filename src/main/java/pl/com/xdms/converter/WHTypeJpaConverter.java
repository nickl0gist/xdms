package pl.com.xdms.converter;

import pl.com.xdms.domain.warehouse.WHTypeEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

/**
 * Created on 23.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Converter(autoApply = true)
public class WHTypeJpaConverter implements AttributeConverter<WHTypeEnum, String> {
    @Override
    public String convertToDatabaseColumn(WHTypeEnum whTypeEnum) {
        if (whTypeEnum == null) {
            return null;
        }
        return whTypeEnum.toString();
    }

    @Override
    public WHTypeEnum convertToEntityAttribute(String type) {
        if (type == null) {
            return null;
        }
        return Stream.of(WHTypeEnum.values())
                .filter(c -> c.getDescription().equals(type))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
