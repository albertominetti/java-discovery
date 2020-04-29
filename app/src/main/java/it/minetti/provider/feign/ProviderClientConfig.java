package it.minetti.provider.feign;

import feign.FeignException;
import feign.Logger;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

import static feign.FeignException.errorStatus;

public class ProviderClientConfig {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(2_000, 10_000, 3);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            FeignException exception = errorStatus(methodKey, response);
            if (HttpStatus.valueOf(response.status()).is5xxServerError()) {
                return new RetryableException(response.status(), response.reason(),
                        response.request().httpMethod(), exception, null, response.request());
            }
            return exception;
        };
    }
}