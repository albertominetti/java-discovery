package it.minetti.app;

import it.minetti.common.EnableWebLoggingInterceptor;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableConfigurationProperties(AppApplication.AppSettings.class)
@Import(EnableWebLoggingInterceptor.class)
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    @Data
    @ConfigurationProperties(prefix = "app.settings")
    public static class AppSettings {
        private int listCount;
    }
}
