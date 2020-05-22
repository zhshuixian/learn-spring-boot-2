package org.xian.webflux.router;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import javax.annotation.Resource;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * @author xiaoxian
 */
@Configuration
public class RouterConfig {
    private final RouterHandler routerHandler;
    @Resource
    RouterFilter filter;

    @Autowired
    public RouterConfig(RouterHandler routerHandler) {
        this.routerHandler = routerHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> routerHandlerConfig() {
        // 设置 Router 的路径为 /helloWebflux,处理 Handler 为 helloWebflux()
        // 设置 Router 的路径为 /helloWebflux,处理 Handler 为 helloWebflux()
        return RouterFunctions.route(GET("/helloWebflux"),
                routerHandler::helloWebflux).filter(filter);
        // .filter() 添加拦截器，andRoute() 添加更多的路径
    }
}
