package it.minetti.provider.feign;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigInteger;

@FeignClient(name = "provider", url = "http://localhost:8081",
        configuration = ProviderClientConfig.class,
        fallback = ProviderClientFallback.class)
public interface ProviderClient {

    @GetMapping("/prime")
    RestResponse retrievePrime();

    @GetMapping("/maybe-prime")
    RestResponse maybeRetrievePrime();

    @Data
    class RestResponse {
        private BigInteger prime;
        private String instanceId;
    }

}
