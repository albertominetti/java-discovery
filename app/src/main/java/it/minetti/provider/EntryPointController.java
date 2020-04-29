package it.minetti.provider;

import it.minetti.provider.feign.ProviderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RefreshScope
@RestController
public class EntryPointController {

    @Autowired
    private ProviderClient providerClient;

    @Autowired
    private AppApplication.AppSettings settings;

    @PostConstruct
    public void setUp() {
        log.info("Initialized with {} list count", settings.getListCount());
    }

    @GetMapping("/")
    public List<BigInteger> retrieveList() {
        log.info("Requesting several prime numbers...");
        List<BigInteger> list = IntStream.rangeClosed(1, settings.getListCount())
                .mapToObj(i -> providerClient.retrievePrime())
                .peek(r -> log.debug("Retrieved from {}", r.getInstanceId()))
                .map(ProviderClient.RestResponse::getPrime)
                .collect(Collectors.toList());
        log.info("...done [{}]", list.size());
        return list;
    }

    @GetMapping("/just-one")
    public BigInteger retrieveOne() {
        log.info("Requesting one prime number");
        ProviderClient.RestResponse response = providerClient.retrievePrime();
        log.info("...retrieved from {}...", response.getInstanceId());
        log.info("...done");
        return response.getPrime();
    }


    @GetMapping("/just-one-lucky")
    public BigInteger retrieveOneLucky() {
        log.info("Requesting one prime number");
        ProviderClient.RestResponse response = providerClient.maybeRetrievePrime();
        log.info("...retrieved from {}...", response.getInstanceId());
        log.info("...done");
        return response.getPrime();
    }
}
