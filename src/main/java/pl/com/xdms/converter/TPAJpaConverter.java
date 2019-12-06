package pl.com.xdms.converter;

import pl.com.xdms.domain.tpa.TPAEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

/**
 * Created on 05.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Converter(autoApply = true)
public class TPAJpaConverter implements AttributeConverter<TPAEnum, String> {
    @Override
    public String convertToDatabaseColumn(TPAEnum tpaEnum) {
        if (tpaEnum == null) {
            return null;
        }
        return tpaEnum.toString();
    }

    @Override
    public TPAEnum convertToEntityAttribute(String tpaEnum) {
        if (tpaEnum == null) {
            return null;
        }
        return Stream.of(TPAEnum.values())
                .filter(c -> c.getCode().equals(tpaEnum))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
