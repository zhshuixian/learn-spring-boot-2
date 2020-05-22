package org.xian.webflux.controller;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xian.webflux.MyMessages;
import org.xian.webflux.service.WebFluxService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author xiaoxian
 */
@RestController
public class WebFluxController {
    @Resource
    WebFluxService service;

    @GetMapping(value = "/hello")
    public Mono<String> hello() {
        return Mono.just("Hello WebFlux By Controller");
    }

    @GetMapping("/getList")
    public Flux<MyMessages> getList() {
        return service.list();
    }

    @GetMapping("/randomNumbers")
    public Flux<ServerSentEvent<Integer>> randomNumbers() {
        // 每次间隔时间 1s
        return Flux.interval(Duration.ofSeconds(1))
                .map(seq -> Tuples.of(seq, ThreadLocalRandom.current().nextInt()))
                .map(data -> ServerSentEvent.<Integer>builder()
                        .event("随机发送信息")
                        .id(Long.toString(data.getT1()))
                        .data(data.getT2())
                        .build()
                );
    }
}
