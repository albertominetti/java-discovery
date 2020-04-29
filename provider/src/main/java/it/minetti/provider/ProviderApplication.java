package it.minetti.provider;

import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(ProviderApplication.ProviderSettings.class)
@SpringBootApplication
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
