package com.scs.issue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookClientHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class Config {

  @Bean
  WebClient webClient(Logbook logbook) {
    return WebClient.builder()
        .baseUrl("https://randomuser.me")
        .clientConnector(
            new ReactorClientHttpConnector(
                HttpClient.create()
                    .doOnConnected(conn -> conn.addHandlerLast(new LogbookClientHandler(logbook)))))
        .build();
  }
}
