package com.study.consumer.infrastructure;

import org.apache.kafka.common.Uuid;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.DockerImageName;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KafkaContainerKraftClusterWithSchemaRegistry implements Startable {

    private final int brokersNum;

    private final String confluentPlatformVersion;

    private final Network network;

    private final List<KafkaContainer> brokers;

    private GenericContainer<?> schemaRegistry;


    public KafkaContainerKraftClusterWithSchemaRegistry(String confluentPlatformVersion, int brokersNum, int internalTopicsRf) {
        if (brokersNum < 0) {
            throw new IllegalArgumentException("brokersNum '" + brokersNum + "' must be greater than 0");
        }
        if (internalTopicsRf < 0 || internalTopicsRf > brokersNum) {
            throw new IllegalArgumentException(
                    "internalTopicsRf '" + internalTopicsRf + "' must be less than brokersNum and greater than 0"
            );
        }

        this.brokersNum = brokersNum;
        this.confluentPlatformVersion = confluentPlatformVersion;
        this.network = Network.newNetwork();

        String controllerQuorumVoters = IntStream
                .range(0, brokersNum)
                .mapToObj(brokerNum -> String.format("%d@broker-%d:9094", brokerNum, brokerNum))
                .collect(Collectors.joining(","));

        String clusterId = Uuid.randomUuid().toString();

        this.brokers =
                IntStream
                        .range(0, brokersNum)
                        .mapToObj(brokerNum -> new KafkaContainer(
                                DockerImageName.parse("confluentinc/cp-kafka").withTag(confluentPlatformVersion))
                                .withNetwork(this.network)
                                .withNetworkAliases("broker-" + brokerNum)
                                .withKraft()
                                .withClusterId(clusterId)
                                .withEnv("KAFKA_BROKER_ID", brokerNum + "")
                                .withEnv("KAFKA_NODE_ID", brokerNum + "")
                                .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", controllerQuorumVoters)
                                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", internalTopicsRf + "")
                                .withEnv("KAFKA_OFFSETS_TOPIC_NUM_PARTITIONS", internalTopicsRf + "")
                                .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", internalTopicsRf + "")
                                .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", internalTopicsRf + "")
                                .withStartupTimeout(Duration.ofMinutes(1)))
                        .collect(Collectors.toList());

        createSchemaRegistry();
    }

    public void createSchemaRegistry() {
        String brokersList = getBrokers().stream()
                .map(broker -> "PLAINTEXT://" + broker.getNetworkAliases().get(0) + ":9092")
                .collect(Collectors.joining(","));
        this.schemaRegistry = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-schema-registry").withTag(confluentPlatformVersion))
                .withNetwork(network)
                .withExposedPorts(8081)
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", brokersList)
                .waitingFor(Wait.forHttp("/subjects").forStatusCode(200));
    }

    public List<KafkaContainer> getBrokers() {
        return this.brokers;
    }

    public String getBootstrapServers() {
        return brokers.stream().map(KafkaContainer::getBootstrapServers).collect(Collectors.joining(","));
    }

    public GenericContainer<?> getSchemaRegistry() {
        return schemaRegistry;
    }

    @Override
    public void start() {
        // Needs to start all the brokers at once
        brokers.parallelStream().forEach(GenericContainer::start);

        Unreliables.retryUntilTrue(60,
                TimeUnit.SECONDS,
                () -> {
                    Container.ExecResult result =
                            this.brokers.stream()
                                    .findFirst()
                                    .get()
                                    .execInContainer(
                                            "sh",
                                            "-c",
                                            "kafka-metadata-shell --snapshot /var/lib/kafka/data/__cluster_metadata-0/00000000000000000000.log ls /brokers | wc -l"
                                    );
                    String brokers = result.getStdout().replace("\n", "");

                    return brokers != null && Integer.valueOf(brokers) == this.brokersNum;
                }
        );
        schemaRegistry.start();
    }

    @Override
    public void stop() {
        this.brokers.stream().parallel().forEach(GenericContainer::stop);
        schemaRegistry.stop();
    }
}
