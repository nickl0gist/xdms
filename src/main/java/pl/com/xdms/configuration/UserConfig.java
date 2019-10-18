package pl.com.xdms.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.com.xdms.filter.UserControllerFilter;

/**
 * Created on 18.10.2019
 * by Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Configuration
public class UserConfig {

    @Autowired
    UserControllerFilter userControllerFilter;

    @Bean
    public FilterRegistrationBean userControllerFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean(userControllerFilter);
        registration.addUrlPatterns("/admin/users/role");
        return registration;
    }

}
