package com.scs.issue;

import brave.http.HttpTracing;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookClientHandler;
import org.zalando.logbook.netty.LogbookServerHandler;
import reactor.netty.http.brave.ReactorNettyHttpTracing;
import reactor.netty.http.client.HttpClient;

@Configuration
public class Config {

  @Bean
  NettyServerCustomizer nettyServerCustomizer(
      Logbook logbook, ReactorNettyHttpTracing reactorNettyHttpTracing) {
    return server ->
        reactorNettyHttpTracing.decorateHttpServer(
            server.doOnConnection(conn -> conn.addHandlerFirst(new LogbookServerHandler(logbook))));
  }

  @Bean
  ReactorNettyHttpTracing reactorNettyHttpTracing(HttpTracing httpTracing) {
    return ReactorNettyHttpTracing.create(httpTracing);
  }

  @Bean
  WebClient webClient(Logbook logbook, ReactorNettyHttpTracing tracing) {
    return WebClient.builder()
        .baseUrl("https://randomuser.me")
        .clientConnector(
            new ReactorClientHttpConnector(
                tracing.decorateHttpClient(
                    HttpClient.create()
                        .doOnConnected(
                            conn -> conn.addHandlerLast(new LogbookClientHandler(logbook))))))
        .build();
  }
}
