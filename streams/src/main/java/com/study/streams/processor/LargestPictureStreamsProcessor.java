package com.study.streams.processor;

import com.study.avromodels.Message;
import com.study.avromodels.Picture;
import com.study.streams.service.LargestPictureService;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.errors.UnknownStateStoreException;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LargestPictureStreamsProcessor {

    public static final String LARGEST_PICTURE_STORE = "largest_picture";
    public static final String BASIC_AUTH_USER_INFO = "basic.auth.user.info";
    private final LargestPictureService largestPictureService;
    private final StreamsBuilderFactoryBean factoryBean;

    @Value("${kafka.push.sol.topic}")
    private String solTopic;

    @Value("${kafka.process.picture.topic}")
    private String pictureTopic;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.properties.basic.auth.credentials.source}")
    private String schemaRegistryCredentialSource;

    @Value("${spring.kafka.properties.basic.auth.user.info}")
    private String schemaRegistryUserInfo;

    @Autowired
    public void process(StreamsBuilder streamsBuilder) {

        KStream<String, Message> solStream = streamsBuilder
                .stream(solTopic);

        solStream.map(this::processMessage)
                .toTable(createMaterialized())
                .toStream()
                .map((key, picture) -> KeyValue.pair(picture.getIpAddress(), picture))
                .to(pictureTopic);
    }

    private KeyValue<Integer, Picture> processMessage(String key, Message message) {
        Picture picture = getLargestPictureFromKafkaStore(message)
                .or(() -> largestPictureService.getLargestPicture(message))
                .orElseGet(() -> getDefaultPicture(message));
        return new KeyValue<>(message.getSol(), picture);
    }

    private Optional<Picture> getLargestPictureFromKafkaStore(Message message) {
        final KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        try {
            ReadOnlyKeyValueStore<Integer, Picture> largestPictureStore = kafkaStreams
                    .store(StoreQueryParameters.fromNameAndType(LARGEST_PICTURE_STORE,
                            QueryableStoreTypes.keyValueStore()));

            Picture picture = largestPictureStore.get(message.getSol());
            if (picture != null) {
                picture.setFullName(message.getFullName());
                picture.setIpAddress(message.getIpAddress());
                log.info("Taken from store: {}", picture);
                return Optional.of(picture);
            }
        } catch (UnknownStateStoreException e) {
            log.error("Could not read from the store: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Picture getDefaultPicture(Message message) {
        Picture defaultPicture = Picture.newBuilder()
                .setFullName(message.getFullName())
                .setUrl("url not found")
                .setSol(message.getSol())
                .setSize(0L)
                .setIpAddress(message.getIpAddress())
                .build();
        log.info("Default picture created: {}", defaultPicture);
        return defaultPicture;
    }

    private Materialized<Integer, Picture, KeyValueStore<Bytes, byte[]>> createMaterialized() {
        return Materialized.<Integer, Picture, KeyValueStore<Bytes, byte[]>>as(LARGEST_PICTURE_STORE)
                .withKeySerde(Serdes.Integer())
                .withValueSerde(getPictureSpecificAvroSerde());
    }

    private SpecificAvroSerde<Picture> getPictureSpecificAvroSerde() {
        final SpecificAvroSerde<Picture> pictureSerde = new SpecificAvroSerde<>();
        Map<String, String> serdeConfig = new HashMap<>();
        serdeConfig.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        serdeConfig.put(AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE, schemaRegistryCredentialSource);
        serdeConfig.put(BASIC_AUTH_USER_INFO, schemaRegistryUserInfo);
        pictureSerde.configure(serdeConfig, false);
        return pictureSerde;
    }
}
