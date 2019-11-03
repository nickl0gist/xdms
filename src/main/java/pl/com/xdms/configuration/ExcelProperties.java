package pl.com.xdms.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

/**
 * Created on 28.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@EnableConfigurationProperties
@PropertySource("classpath:excel.properties")
@ConfigurationProperties(prefix = "referencebase")
@Data
public class ExcelProperties {
    @Value("${referencebase.template}")
    private String pathToReferenceTemplate;
}
