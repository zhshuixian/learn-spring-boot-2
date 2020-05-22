package org.xian.webflux.router;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;


@Component
class RouterFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    @Override
    public Mono<ServerResponse> filter(ServerRequest serverRequest,
                                       HandlerFunction<ServerResponse> handlerFunction) {
        // TODO 拦截判断处理 通过使用 return next.handle(request);
        // 不通过使用如下
        return ServerResponse.status(UNAUTHORIZED).body(Mono.just("被拦截了"), String.class);
    }
}