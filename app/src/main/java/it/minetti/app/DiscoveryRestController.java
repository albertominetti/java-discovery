package it.minetti.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DiscoveryRestController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/discovery")
    public Map<String, List<ServiceInstance>> discovery() {
        Map<String, List<ServiceInstance>> objectObjectHashMap = new HashMap<>();
        List<String> services = discoveryClient.getServices();

        for (String service : services) {
            objectObjectHashMap.put(service, discoveryClient.getInstances(service));
        }
        return objectObjectHashMap;
    }
}
