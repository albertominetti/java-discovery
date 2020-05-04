package it.minetti.app;

import it.minetti.app.feign.ProviderClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ActiveProfiles("smoke")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AppApplicationIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    ProviderClient providerClient;

    @LocalServerPort
    private Integer port;

    @Test
    public void smoke() {
        // given
        ProviderClient.RestResponse restResponse = new ProviderClient.RestResponse();
        restResponse.setPrime(BigInteger.TEN);
        when(providerClient.retrievePrime()).thenReturn(restResponse);

        // when
        BigInteger response = restTemplate.getForObject("http://localhost:" + port + "/just-one", BigInteger.class);

        // then
        assertThat(response, is(comparesEqualTo(BigInteger.TEN)));
    }

}