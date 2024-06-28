package com.study.consumer.consumer;

import com.study.avromodels.Picture;
import com.study.consumer.mapper.PictureDto;
import com.study.consumer.mapper.PictureMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PictureConsumer {

    private final PictureMapper pictureMapper;
    private final Map<String, Set<Picture>> ipToPictureMap = new HashMap<>();

    @KafkaListener(topics = "${kafka.process.picture.topic}")
    public void consume(ConsumerRecord<String, Picture> consumerRecord) {
        String address = consumerRecord.key();
        Picture picture = consumerRecord.value();
        ipToPictureMap.computeIfAbsent(address, k -> new HashSet<>()).add(picture);
        log.info("Consumed record with key: {} and value: {}", address, picture);
    }

    public List<PictureDto> getAllByAddress(String key) {
        return Optional.ofNullable(ipToPictureMap.get(key))
                .stream()
                .flatMap(list -> list.stream()
                        .map(pictureMapper::pictureToDto))
                .toList();
    }
}

