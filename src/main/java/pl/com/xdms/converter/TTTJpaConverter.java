package pl.com.xdms.converter;

import pl.com.xdms.domain.trucktimetable.TTTEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

/**
 * Created on 05.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Converter(autoApply = true)
public class TTTJpaConverter implements AttributeConverter<TTTEnum, String> {

    @Override
    public String convertToDatabaseColumn(TTTEnum tttEnum) {
        if (tttEnum == null) {
            return null;
        }
        return tttEnum.toString();
    }

    @Override
    public TTTEnum convertToEntityAttribute(String tttStatus) {
        if (tttStatus == null) {
            return null;
        }
        return Stream.of(TTTEnum.values())
                .filter(c -> c.getCode().equals(tttStatus))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
