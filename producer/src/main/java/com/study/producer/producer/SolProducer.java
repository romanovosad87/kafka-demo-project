package com.study.producer.producer;





import com.study.avromodels.Message;
import com.study.producer.controller.InputData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class SolProducer {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    @Value("${kafka.push.topic}")
    private String topic;

    public void send(InputData data, String address) {
        Message message = Message.newBuilder()
                .setSol(data.sol())
                .setIpAddress(address)
                .setFullName(data.fullName())
                .build();
        kafkaTemplate.send(topic, message);
        log.info("The message {} was asynchronously sent to Kafka", message);
    }
}
