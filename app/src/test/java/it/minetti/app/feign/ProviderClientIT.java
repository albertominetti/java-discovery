package it.minetti.app.feign;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import feign.FeignException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("feign")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {ProviderClientIT.WireMockInitializer.class})
public class ProviderClientIT {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    ProviderClient providerClient;

    @LocalServerPort
    private Integer port;

    @BeforeEach
    public void beforeEach() {
        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.resetAll();
    }

    @Test
    public void retrievePrime_succesfull() {
        // given
        wireMockServer.stubFor(get("/prime")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"prime\": 123456789,\"provider\": \"something\"}"))
        );

        // when
        ProviderClient.RestResponse restResponse = providerClient.retrievePrime();

        // then
        assertThat(restResponse, is(notNullValue()));
        assertThat(restResponse.getPrime(), is(comparesEqualTo(new BigInteger("123456789"))));
        verify(exactly(1), getRequestedFor(urlEqualTo("/prime")));
    }

    @Test
    public void maybeRetrievePrime_succesfull_with_retries() {
        // given
        wireMockServer.stubFor(get("/maybe-prime")
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Cause fail Again")
        );

        wireMockServer.stubFor(get("/maybe-prime")
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Cause fail Again")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Cause Success")
        );

        wireMockServer.stubFor(get("/maybe-prime")
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Cause Success")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"prime\": 123456789,\"provider\": \"something\"}"))
        );

        // when
        ProviderClient.RestResponse restResponse = providerClient.maybeRetrievePrime();

        // then
        assertThat(restResponse, is(notNullValue()));
        assertThat(restResponse.getPrime(), is(comparesEqualTo(new BigInteger("123456789"))));
        verify(exactly(3), getRequestedFor(urlEqualTo("/maybe-prime")));
    }


    @Test
    public void maybeRetrievePrime_fail() {
        // given
        wireMockServer.stubFor(get("/maybe-prime")
                .willReturn(aResponse().withStatus(500))
        );

        // when
        Executable executable = () -> providerClient.maybeRetrievePrime();

        // then
        assertThrows(FeignException.class, executable);
        verify(exactly(3), getRequestedFor(urlEqualTo("/maybe-prime")));
    }

    public static class WireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
            wireMockServer.start();

            configurableApplicationContext.getBeanFactory().registerSingleton("wireMockServer", wireMockServer);

            configurableApplicationContext.addApplicationListener(applicationEvent -> {
                if (applicationEvent instanceof ContextClosedEvent) {
                    wireMockServer.stop();
                }
            });

        }
    }

    @TestConfiguration
    public static class RibbonConfiguration {
        @Bean
        public ServerList<Server> ribbonServerList(WireMockServer wireMockServer) {
            return new StaticServerList<>(new Server("localhost", wireMockServer.port()));
        }
    }
}