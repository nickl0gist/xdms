package pl.com.xdms.converter;

import pl.com.xdms.domain.user.RoleEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

/**
 * Created on 16.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Converter(autoApply = true)
public class RoleJpaConverter implements AttributeConverter<RoleEnum, String> {
    @Override
    public String convertToDatabaseColumn(RoleEnum roleEnum) {
        if (roleEnum == null) {
            return null;
        }
        return roleEnum.toString();
    }

    @Override
    public RoleEnum convertToEntityAttribute(String role) {
        if (role == null) {
            return null;
        }
        return Stream.of(RoleEnum.values())
                .filter(c -> c.getCode().equals(role))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
