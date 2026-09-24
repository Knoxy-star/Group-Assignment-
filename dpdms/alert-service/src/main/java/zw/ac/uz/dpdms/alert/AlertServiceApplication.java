package zw.ac.uz.dpdms.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import zw.ac.uz.dpdms.alert.config.AlertProperties;
import zw.ac.uz.dpdms.alert.config.WhatsAppProperties;

/**
 * @ComponentScan lists both this service's package and
 * zw.ac.uz.dpdms.common, so RequestContextResolver is found.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties({AlertProperties.class, WhatsAppProperties.class})
@ComponentScan(basePackages = {"zw.ac.uz.dpdms.alert", "zw.ac.uz.dpdms.common"})
public class AlertServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlertServiceApplication.class, args);
    }
}
