package ru.shop.backend.search.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableElasticsearchRepositories
@EnableScheduling
public class AppConfig {
    @Value("${spring.elasticsearch.rest.uris}")
    private String elasticUrl;
    @Bean
    public ClientConfiguration clientConfiguration(){
        return ClientConfiguration.builder().connectedTo(elasticUrl)
                .build();
    }
    @Bean
    public RestHighLevelClient restHighLevelClient(ClientConfiguration client){
        return RestClients.create(client).rest();
    }

}
