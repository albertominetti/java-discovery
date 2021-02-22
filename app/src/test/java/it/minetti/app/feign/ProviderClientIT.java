package it.minetti.app.feign;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import feign.FeignException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("feign")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class ProviderClientIT {

    static WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());

    @Autowired
    ProviderClient providerClient;

    @BeforeAll
    static void setUp() {
        wireMockServer.start();
    }

    @BeforeEach
    void beforeEach() {
        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void afterEach() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void cleanUp() {
        wireMockServer.stop();
    }

    @Test
    void retrievePrime_succesfull() {
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
    void maybeRetrievePrime_succesfull_with_retries() {
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
    void maybeRetrievePrime_fail() {
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

    @TestConfiguration
    public static class RibbonConfiguration {
        @Bean
        public ServerList<Server> ribbonServerList() {
            return new StaticServerList<>(new Server("localhost", wireMockServer.port()));
        }
    }
}