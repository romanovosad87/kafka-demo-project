package com.study.producer.cluster;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClusterInfo {

    private final Environment env;

    @SneakyThrows
    @EventListener(ContextRefreshedEvent.class)
    public void getClusterConfigs() {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("spring.kafka.bootstrap-servers"));
        properties.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, env.getProperty("spring.kafka.properties.security.protocol"));
        properties.put("sasl.mechanism", env.getProperty("spring.kafka.properties.sasl.mechanism"));
        properties.put("sasl.jaas.config", env.getProperty("spring.kafka.properties.sasl.jaas.config"));

        try (AdminClient adminClient = AdminClient.create(properties)) {

            DescribeClusterResult cluster = adminClient.describeCluster();

            log.info("Connected to cluster: {}", cluster.clusterId().get());
            log.info("The brokers in the cluster are:");
            cluster.nodes().get().forEach(node -> log.info(" * {}", node));
            log.info("The controller is: {}", cluster.controller().get());
        }
    }
}
