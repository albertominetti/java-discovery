package it.minetti.provider.feign;

import org.springframework.stereotype.Component;

@Component
public class ProviderClientFallback implements ProviderClient {

    @Override
    public RestResponse retrievePrime() {
        return new RestResponse();
    }

    @Override
    public RestResponse maybeRetrievePrime() {
        return new RestResponse();
    }
}
