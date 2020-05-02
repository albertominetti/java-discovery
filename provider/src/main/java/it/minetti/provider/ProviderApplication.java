package it.minetti.provider;

import it.minetti.common.EnableWebLoggingInterceptor;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@EnableConfigurationProperties(ProviderApplication.ProviderSettings.class)
@SpringBootApplication
@Import(EnableWebLoggingInterceptor.class)
public class ProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProviderApplication.class, args);
	}

	@Data
	@ConfigurationProperties(prefix = "provider.settings")
	public static class ProviderSettings {
		private int primeSize;
		private String instanceId;
	}
}
