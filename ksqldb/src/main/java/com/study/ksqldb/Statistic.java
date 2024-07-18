package com.study.ksqldb;

import com.study.ksqldb.dto.StatisticDto;
import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ExecuteStatementResult;
import io.confluent.ksql.api.client.Row;
import io.confluent.ksql.api.client.StreamedQueryResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class Statistic {

    private static final String STREAM_NAME = "pictures";
    private static final String TABLE_NAME = "statistic";
    private static final Map<String, Object> PROPERTIES = Collections.singletonMap(
            "auto.offset.reset", "earliest"
    );

    @Value("${kafka.process.picture.topic}")
    private String kafkaTopic;

    private final Client ksqlClient;

    @EventListener(ContextRefreshedEvent.class)
    public void createStream() {
        String query = """
                CREATE STREAM IF NOT EXISTS %s (
                ip_address varchar(255) KEY ,
                url varchar(50) ,
                `size` bigint ,
                sol int
                ) WITH (
                kafka_topic = '%s',
                value_format = 'avro'
                );
                """.formatted(STREAM_NAME, kafkaTopic);


        executeKsqlStatement(query)
                .thenCompose(result -> createTable())
                .exceptionally(ex -> {
                    log.error("Failed to execute statement: {}", ex.getMessage());
                    return null;
                });
    }

    private CompletableFuture<ExecuteStatementResult> createTable() {
        String createTable = """
                CREATE TABLE IF NOT EXISTS %s AS
                SELECT ip_address, COUNT(*) as total_requests
                FROM %s
                GROUP BY ip_address
                EMIT CHANGES;
                """.formatted(TABLE_NAME, STREAM_NAME);

        return  executeKsqlStatement(createTable);
    }

    @SneakyThrows
    public List<StatisticDto> getStatistic() {
        String selectAllPullQuery = "SELECT * FROM %s;".formatted(TABLE_NAME);
        StreamedQueryResult result = ksqlClient.streamQuery(selectAllPullQuery, PROPERTIES)
                .get();

        List<StatisticDto> pictureEntities = new ArrayList<>();
        Row row;
        while ((row = result.poll()) != null) {
            pictureEntities.add(mapToDto(row));
        }
        return pictureEntities;
    }

    @SneakyThrows
    public StatisticDto getStatisticByAddress(String ipAddress) {
        String selectStatisticByAddressPullQuery = "SELECT * FROM %s WHERE IP_ADDRESS='%s';"
                .formatted(TABLE_NAME, ipAddress);
        StreamedQueryResult result = ksqlClient.streamQuery(selectStatisticByAddressPullQuery)
                .get();

        return Optional.ofNullable(result.poll())
                .map(this::mapToDto)
                .orElseGet(() -> new StatisticDto(ipAddress, 0L));
    }


    private StatisticDto mapToDto(Row row) {
        StatisticDto dto = new StatisticDto();
        dto.setIpAddress(row.getString("IP_ADDRESS"));
        dto.setTotalRequests(row.getLong("TOTAL_REQUESTS"));

        return dto;
    }

    private CompletableFuture<ExecuteStatementResult> executeKsqlStatement(String query) {
        return ksqlClient.executeStatement(query, PROPERTIES)
                .thenApply(result -> {
                    log.info("Statement executed successfully: {}", result);
                    return result;
                })
                .exceptionally(ex -> {
                    log.error("Failed to execute statement: {}", ex.getMessage());
                    return null;
                });
    }
}
