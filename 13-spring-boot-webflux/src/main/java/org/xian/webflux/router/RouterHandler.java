package org.xian.webflux.router;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.xian.webflux.MyMessages;
import reactor.core.publisher.Mono;

/**
 * @author xiaoxian
 */
@Component
public class RouterHandler {

    public Mono<ServerResponse> helloWebflux(ServerRequest request) {
        // 前端请求数据从 Request 获取
        // 设置返回码为 200 ok
        return ServerResponse.ok()
                // 设置返回 格式 UTF8 JSON
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                // 设置返回 body 的内容
                .body(Mono.just(new MyMessages("OK", "From WebFlux ! From " + request.path())),
                        MyMessages.class);
    }
}
