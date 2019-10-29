package pl.com.xdms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.com.xdms.configuration.ExcelProperties;
import pl.com.xdms.configuration.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class,
		ExcelProperties.class
})
public class XdmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(XdmsApplication.class, args);
	}

}
