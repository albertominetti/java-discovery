# Microservices with discovery

## Goal

The idea behind this project is to create a simple and working infrastructure to get the best from the Feign declarative REST client. The topic of this demo is related to: auto-discovery, ribbon retry, endpoint retry, centralized configuration, configuration refresh, tracing and centralized logging.

## How to build and run

A running docker machine is mandatory for setup the whole environment, so if the docker machine is not running you can use the following command:

```sh
docker-machine start && docker-machine env
```

The following command builds and run the infrastructure, a prerequisite for the environment of microservices. The docker-compose-infra.yml includes:
* Consul engine that provides discovery, health check and shared configurations
* Splunk that collects logs and allows to search for them

```sh
docker-compose -f docker-compose-infra.yml up --build -d
```

There are two separate docker file, one for each microservice. Both services need to be compiled with maven to generate the executable jar in their corresponding target folder. Then the docker-compose is responsible to build and run the microservices with a proper scaling
 
```sh
mvn clean package -DskipTests && 
  docker-compose -f docker-compose-provider.yml up --build -d --scale provider=3  &&
  docker-compose -f docker-compose-app.yml up --build -d
```

For simplicity you can build and run all together:

```sh
mvn clean package -DskipTests && 
  docker-compose -f docker-compose-infra.yml -f docker-compose-provider.yml -f docker-compose-app.yml up --build -d --scale provider=3
```

#### Running environment     

Use the following to check the active instances of microservices and infrastructure components. That specific format can be useful to retrieve the specific more important info.

```sh
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

The output expected is the following:

```
NAMES               STATUS                    PORTS
provider-log-fw_1   Up 22 minutes (healthy)   8088-8089/tcp, 9997/tcp
app-log-fw_1        Up 22 minutes (healthy)   8088-8089/tcp, 9997/tcp
provider_3          Up 22 minutes             0.0.0.0:8087->8080/tcp
splunk              Up 22 minutes (healthy)   8065/tcp, 8088-8089/tcp, 8191/tcp, 9887/tcp, 0.0.0.0:8000->8000/tcp, 9997/tcp
provider_2          Up 22 minutes             0.0.0.0:8086->8080/tcp
app                 Up 22 minutes             0.0.0.0:8080->8080/tcp
consul              Up 22 minutes             8300-8302/tcp, 8600/tcp, 8301-8302/udp, 0.0.0.0:8500->8500/tcp, 0.0.0.0:8600->8600/udp
provider_1          Up 22 minutes             0.0.0.0:8085->8080/tcp
```

To stop and destroy the whole infrastructure use the following command

```sh
docker-compose -f docker-compose-infra.yml -f docker-compose-provider.yml -f docker-compose-app.yml down -v
```

## The infrastructure

The urls may vary depending you own docker machine configuration, mine is located at `192.168.99.100`. Change the ip to match with your own configuration.

### Consul
The main page to retrieve details about the microservice is at the following url, there you can check the services, instances and the configuration. It is also possible to change dynamically the two key/value properties read by the microservices.

* http://192.168.99.100:8500

To get more details about the services this REST API can be very useful

* http://192.168.99.100:8500/v1/agent/checks

### Splunk

The Splunk interface is available here, user is `admin` and password is `password` 

http://192.168.99.100:8000

It allows to query the logs in a single place, it scrapes the distributed log directories for you thanks to the splunk universal forwarder. An example of a query is shown here:

http://192.168.99.100:8000/it-IT/app/search/search?q=search%20service%20%3D%20%22provider%22%20OR%20service%20%3D%20%22app%22&display.page.search.mode=smart&dispatch.sample_ratio=1&workload_pool=&earliest=rt-5m&latest=rt&display.events.type=table&display.events.fields=%5B%22service%22%2C%22severity%22%2C%22logger%22%2C%22message%22%5D&display.prefs.events.count=50&sid=rt_1588348109.34

## Microservices

The project contains two spring boot microservices, both are using almost the same dependencies hosted in the parent pom.xml file. A specific common module provides the shared configuration. The dependencies in the parent and the common module are intended as a framework that all the applications has to depend on to enable the basic features and to simplify the development.

### The provider application

The provider side contains a RestController with two endpoints: one produce a prime number the second one do the same but may randomly fail, the purpose of this behavior is to simulate outages. Because of the provider application can scale up, the accessible port may vary between 8085 and 8095. This are the two endpoints:

* http://192.168.99.100:8087/prime
* http://192.168.99.100:8087/maybe-prime

### The main application

Under the module app there is the main application, it uses the feign client to interact with the provider. There are two endpoints that directly interact with the two endpoint of provider described just before

* http://192.168.99.100:8080/just-one
* http://192.168.99.100:8080/just-one-lucky

The latter queries the provider, if the reply from the provider is http status 500 (pretending to be an outage) the application will use implicitly the ribbon balancer to retry the query using another provider instance in a round robin way. The interval is configured by an exponential backoff to prevent too many queries to a system in outage that can be too heavy loaded.

There is another endpoint to retrieve a full list of data from the provider, it can be dynamically configured in term of list size using the property in Consul

* http://192.168.99.100:8080/

---

## Final notes

There are several point that are present just for a matter of sample, and are not intended to bring to an enterprise production environment. 

The retry mechanism is not always a good idea, despite the useful exponential backoff, continuing to query an overloaded system may lead to decrease the performances of it; would be better to setup a proper circuit breaker pattern using `hystrix` or `resilience4j`.

Retry can be done querying multiple times the same provider instance (implemented by `Retryer`) or try with the next instance each attempt (implemented by `LoadBalancedRetryFactory`), both configuration are present in the application but just one is really needed.

In the main application there is just a single feign client, so does not matter if its own configuration are specific (using the `configuration` property of the `@FeignClient`) or global (annotating the configuration using `@Configuration`).

Splunk is a very powerful tool, no license is required if the daily log data is below 500MB, with more data a licence is needed. There are free alternatives like `Kibana` and `graylog` but are not so complete in my opinion.

I choose to use the splunk universal forwarder, but also `fluentd` and `GELF` can be very good alternatives.

All the environment can run using docker, as I implemented, but the actual configuration is very far from an actual production ready configuration.

