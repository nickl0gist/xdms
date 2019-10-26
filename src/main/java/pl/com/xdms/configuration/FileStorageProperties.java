package pl.com.xdms.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created on 26.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
