package com.study.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.study.avromodels.Picture;
import com.study.consumer.consumer.PictureConsumer;
import com.study.consumer.infrastructure.KafkaContainerKraftClusterWithSchemaRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
class ConsumerApplicationTests {

    private static final int BROKERS_NUMBER = 3;
    private static final int INTERNAL_TOPICS_REPLICATION_FACTOR = 2;
    public static final String CONFLUENT_PLATFORM_VERSION = "7.4.0";
    private static KafkaContainerKraftClusterWithSchemaRegistry cluster;

    @Autowired
    private KafkaTemplate<String, Picture> kafkaTemplate;

    @SpyBean
    private PictureConsumer pictureConsumer;

    @Value("${kafka.process.picture.topic}")
    private String topic;

    @BeforeAll
    static void beforeAll() {
        cluster = new KafkaContainerKraftClusterWithSchemaRegistry(CONFLUENT_PLATFORM_VERSION,
                BROKERS_NUMBER, INTERNAL_TOPICS_REPLICATION_FACTOR);
        cluster.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        String schemaRegistryUrl = "http://%s:%s".formatted(cluster.getSchemaRegistry().getHost(),
                cluster.getSchemaRegistry().getFirstMappedPort());
        registry.add("spring.kafka.bootstrap-servers", cluster::getBootstrapServers);
        registry.add("spring.kafka.properties.security.protocol", () -> "PLAINTEXT");
        registry.add("spring.kafka.properties.schema.registry.url", () -> schemaRegistryUrl);
    }

    @Test
    void test() {

        // Arrange
        String fullName = "Jay Kreps";
        String ipAddress = "127.0.0.1";
        int sol = 23;
        long size = 125L;
        String url = "http://nasa.largest";
        Picture picture = Picture.newBuilder()
                .setSol(sol)
                .setFullName(fullName)
                .setIpAddress(ipAddress)
                .setSize(size)
                .setUrl(url)
                .build();

        // Act
        kafkaTemplate.send(topic, picture);

        // Assert
        ArgumentCaptor<ConsumerRecord<String, Picture>> captor = ArgumentCaptor.forClass(ConsumerRecord.class);

        verify(pictureConsumer, Mockito.timeout(5000).times(1))
                .consume(captor.capture());

        Picture consumedPicture = captor.getValue().value();
        assertThat(consumedPicture).isEqualTo(picture);
    }

    @AfterAll
    static void afterAll() {
        cluster.stop();
    }
}
