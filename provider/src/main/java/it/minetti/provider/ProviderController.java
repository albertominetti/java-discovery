package it.minetti.provider;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.Random;

@Slf4j
@RefreshScope
@RestController
public class ProviderController {

    private final Random random = new Random();

    @Autowired
    private ProviderApplication.ProviderSettings settings;

    @GetMapping("/prime")
    public Response generate() {
        log.info("Generating a new prime with {} bits...", settings.getPrimeSize());
        Response response = new Response(new BigInteger(settings.getPrimeSize(), 9, random));
        log.info("...done");
        response.setInstanceId(settings.getInstanceId());
        return response;
    }

    @GetMapping("/maybe-prime")
    public Response maybegGenerate() {
        if (random.nextInt(2) == 0) {
            throw new UnsupportedOperationException("Better luck next time.");
        }
        return generate();
    }

    @Data
    public static class Response {
        private final BigInteger prime;
        private String instanceId;

        @JsonCreator
        // better and implicit way: https://github.com/FasterXML/jackson-modules-java8/tree/master/parameter-names
        public Response(@JsonProperty("prime") BigInteger prime) {
            this.prime = prime;
        }
    }
}
