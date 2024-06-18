package com.study.streams.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.avromodels.Message;
import com.study.avromodels.Picture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class LargestPictureServiceImpl implements LargestPictureService {

    public static final String SOL = "sol";
    public static final String API_KEY = "api_key";
    public static final String IMG_SRC = "img_src";

    @Value("${nasa.client.key}")
    private String nasaKey;

    @Value("${nasa.client.url}")
    private String nasaUrl;

    @Override
    public Optional<Picture> getLargestPicture(Message message) {
        return WebClient.builder()
                .codecs(codecConfigurer -> codecConfigurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .baseUrl(buildUrl(message.getSol()))
                .build()
                .get()
                .exchangeToMono(resp -> resp.bodyToMono(Photos.class))
                .map(Photos::photos)
                .flatMapMany(Flux::fromIterable)
                .map(Photo::imgSrc)
                .flatMap(pictureUrl -> getPicture(message, pictureUrl))
                .reduce((p1, p2) -> p1.getSize() > p2.getSize() ? p1 : p2)
                .doOnNext(picture -> log.info("The picture is retrieved from NASA: {}", picture))
                .blockOptional(Duration.ofSeconds(30));
    }

    private Mono<Picture> getPicture(Message message, String pictureUrl) {
        return WebClient.create(pictureUrl)
                .head()
                .exchangeToMono(ClientResponse::toBodilessEntity)
                .map(HttpEntity::getHeaders)
                .map(httpHeaders -> httpHeaders.getLocation().toString())
                .flatMap(redirectUrl -> WebClient.create(redirectUrl)
                        .head()
                        .exchangeToMono(ClientResponse::toBodilessEntity)
                        .map(respEntity -> respEntity.getHeaders().getContentLength())
                        .map(length -> new Picture(message.getSol(), message.getFullName(),
                                pictureUrl, length, message.getIpAddress())));
    }

    private String buildUrl(int sol) {
        return UriComponentsBuilder.fromHttpUrl(nasaUrl)
                .queryParam(SOL, sol)
                .queryParam(API_KEY, nasaKey)
                .build()
                .toString();
    }
}

record Photo(@JsonProperty(LargestPictureServiceImpl.IMG_SRC) String imgSrc){}

record Photos(List<Photo> photos){}