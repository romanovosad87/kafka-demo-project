package com.study.ksqldb.config;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${ksqldb.host}")
    private String ksqldbHost;

    @Value("${ksqldb.username}")
    private String ksqldbUsername;

    @Value("${ksqldb.password}")
    private String ksqldbPassword;

    @Bean
    public Client createKsqlClient() {
        var options =  ClientOptions.create()
                .setHost(ksqldbHost)
                .setPort(443)
                .setBasicAuthCredentials(ksqldbUsername, ksqldbPassword)
                .setUseTls(true)
                .setUseAlpn(true);
        return Client.create(options);
    }
}
