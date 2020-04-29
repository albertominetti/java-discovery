package it.minetti.provider.feign;

import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancedRetryFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;

@Configuration
public class LoadBalancerRetryConfiguration {

    @Bean
    public LoadBalancedRetryFactory loadBalancerRetryFactory(SpringClientFactory clientFactory) {
        return new RibbonLoadBalancedRetryFactory(clientFactory) {
            @Override
            public BackOffPolicy createBackOffPolicy(String service) {
                return new ExponentialRandomBackOffPolicy();
            }
        };
    }


}