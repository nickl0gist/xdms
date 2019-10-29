package pl.com.xdms.beans;

import lombok.Data;
import org.springframework.context.annotation.PropertySource;

/**
 * Created on 28.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */

@Data
@PropertySource("classpath:excel.properties")

public class ReferenceBase {
//    private LinkedHashMap<String, Integer> columns;
//    @Value("${reference-base.sheetName}")
//    private String sheetName;
}

