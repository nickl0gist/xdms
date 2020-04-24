package pl.com.xdms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.com.xdms.configuration.ExcelProperties;
import pl.com.xdms.configuration.FileStorageProperties;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class,
		ExcelProperties.class
})
@EnableSwagger2
public class XdmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(XdmsApplication.class, args);
	}

}
