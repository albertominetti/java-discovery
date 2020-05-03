package it.minetti.provider.feign;

import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCircuitBreaker
@EnableDiscoveryClient
@EnableFeignClients(basePackageClasses = FeignConfig.class)
public class FeignConfig {

    @Bean
    public Decoder feignDecoder() {
        return new SpringDecoder(HttpMessageConverters::new);
    }

    @Bean
    public Encoder feignEncoder() {
        return new SpringEncoder(HttpMessageConverters::new);
    }

}
