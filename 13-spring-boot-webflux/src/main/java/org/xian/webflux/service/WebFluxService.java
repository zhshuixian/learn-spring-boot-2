package org.xian.webflux.service;

import org.springframework.stereotype.Service;
import org.xian.webflux.MyMessages;
import reactor.core.publisher.Flux;

/**
 * @author xiaoxian
 */
@Service
public class WebFluxService {

    public Flux<MyMessages> list() {
        MyMessages[] myMessages = new MyMessages[2];
        // TODO 查询数据库，MySQL 等 SQL 数据库暂不支持 Reactive，
        // 操作数据的方式参考 Spring Data JPA 部分，只不过将结果使用 Mono、Flux封装
        myMessages[0] = new MyMessages("ok", "Message 1");
        myMessages[1] = new MyMessages("ok", "Message 2");
        return Flux.fromArray(myMessages);
    }
}
