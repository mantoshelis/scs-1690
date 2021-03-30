package com.scs.issue;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class Function {

  private final WebClient webClient;

  @Bean
  RouterFunction<ServerResponse> userRoute() {
    return route().POST("/users", this::createUser).build();
  }

  private Mono<ServerResponse> createUser(ServerRequest serverRequest) {
    return serverRequest
        .bodyToMono(CreateUserRequest.class)
        .zipWith(getUsername())
        .doOnSuccess(
            tuple -> {
              var request = tuple.getT1();
              var username = tuple.getT2();
              log.info("Creating user {} with request: {}", username, request);
            })
        .then(ServerResponse.noContent().build());
  }

  private Mono<String> getUsername() {
    return webClient
        .get()
        .uri("/api?results=1")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(RandomUsersResponse.class)
        .map(response -> response.getResults().get(0).getLogin().getUsername());
  }

  @Data
  @Accessors(chain = true)
  private static class RandomUsersResponse {

    private List<RandomUser> results = new ArrayList<>();
  }

  @Data
  @Accessors(chain = true)
  private static class RandomUser {

    private Login login;

    @Data
    @Accessors(chain = true)
    private static class Login {

      private String username;
    }
  }

  @Data
  @Accessors(chain = true)
  private static class CreateUserRequest {
    private String password;
  }
}
