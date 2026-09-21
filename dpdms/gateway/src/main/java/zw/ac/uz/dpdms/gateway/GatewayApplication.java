package zw.ac.uz.dpdms.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * DPDMS API Gateway.
 *
 * Single entry point for the front end and any external client.
 * Routes requests to the correct downstream service by looking them
 * up in Eureka (see discovery-service), so nobody needs to remember
 * which port flood-service happens to be running on.
 *
 * The JwtAuthenticationFilter (in the filter package) runs on every
 * request BEFORE it is routed downstream, and stamps the caller's
 * role, ward and hazard scope onto request headers. Downstream
 * services still re-check authorization themselves (per the brief:
 * scoping must be enforced in the backend, never only at the edge) -
 * the gateway filter is a first line of defence, not the only one.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
