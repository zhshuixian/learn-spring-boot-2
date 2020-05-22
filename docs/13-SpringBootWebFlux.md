# Spring Boot 2.X 实战--WebFulx Reactive 编程初探

> 源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)
>
> 码云：https://gitee.com/ylooq/learn-spring-boot-2
>
> 博客：https://blog.csdn.net/u010974701

Spring 5 中最重要的更新是支持 Reactive （反应式）编程，反应式编程是非阻塞的，意味着无需阻塞等待业务处理完成，可以减少服务器资源的占用，提高并发处理速度和并发量。非常适合低延迟、高吞吐量的项目。可以用来构建非阻塞的、异步的、弹性和事件驱动的企业级服务。

Spring WebFlux 是 Spring 5 新增的框架，是一个用于开发函数式 Reactive Web 的框架，能够使用更少的资源完成更多的任务，提高系统效率，能够处理更多的并发连接，极大地提高系统处理能力。

这里将简要介绍 Spring Boot 整合 WebFlux 实现简单功能开发。

> 反应式编程是一种基于 **数据流** 和 **变化传递** 的 **声明式**的编程范式。

## 1、Spring WebFlux 简介

![WebFlux 功能模块图](https://gitee.com//ylooq/image-repository/raw/master/image2020/20200515001225.png)

WebFlux 需要运行在支持 Servlet 3.1+ 的容器上或者其他支持异步处理的容器上。Spring WebFlux 支持的容器有 Tomcat、Jetty、**Netty（默认容器）**、Undertow 等。

### 1.1、WebFlux 功能模块

WebFlux 主要由三个模块组成：

#### 1.1.1、Router Function

提供函数式的 API 接口，用于创建 Router（路由，即 API）、Handler（业务处理）、Filter（拦截器），另外，WebFlux 还支持标准的 `@Controller`，`@RequestMapping`等的 `Spring MVC` 注解创建 API 接口。

```java
// 函数式编程创建 API 接口 ，指定 API 接口路径、请求方法和业务处理 Handler
@Bean
public RouterFunction<ServerResponse> routerHandlerConfig() {
    return RouterFunctions.route(GET("/helloWebflux"),
            routerHandler::helloWebflux);
}
```

#### 1.1.2、Spring WebFlux

核心组件，协调上下游各个组件提供 **响应式编程** 支持。WebFlux 会把数据流封装成  Mono 或 Flux 格式进行统一处理。  其中 Mono 和 Flux 是 **事件发布者**，当事件发生后，会回调相应的方法来通知客户端，。

- Mono ： 处理单个 Item
- Flux ： 可以处理多个 Item

#### 1.1.3、Reactive Streams

一种支持 背压（Backpressure）的异步数据流处理标准，只要实现的反应式框架有 RxJava、Reactor 等。WebFlux 默认集成的是 Reactor。

## 2、开始使用

跟其他 Spring 框架一样，WebFlux 对 Spring Boot 也提供开箱即用启动器（starter），*spring-boot-starter-webflux*,使得开发人员可以快速的集成和开发 Reactive Web 应用。

![](https://gitee.com//ylooq/image-repository/raw/master/image2020/20200514233029.png)

新建项目 *14-spring-boot-webflux* ，注意引入的 **Spring Reactive Web** 依赖模块,引入如下依赖：

```json
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    testImplementation('org.springframework.boot:spring-boot-starter-test') {
        exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
    }
    testImplementation 'io.projectreactor:reactor-test'
}
```

或者;

```xml
 <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
      <exclusions>
        <exclusion>
          <groupId>org.junit.vintage</groupId>
          <artifactId>junit-vintage-engine</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
    <dependency>
      <groupId>io.projectreactor</groupId>
      <artifactId>reactor-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
```

MyMessages.java

```java
public class MyMessages {
    private String status;
    private String message;
    // 省略 Getter setter、构造函数
```

运行项目，可以看到 WebFlux 默认使用 Netty 容器：

```bash
netty.NettyWebServer  : Netty started on port(s): 8080
```

### 2.1、Router Function

上面提到 Router Function 的三个组件：Router、Handler、Filter，这里演示如何使用 Router 和 Handler 构建 RESTful API，新建 RouterHandler.class

```java
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
```

使用 Router 将 Handler 绑定到 api：

```java
@Configuration
public class RouterConfig {
    private final RouterHandler routerHandler;

    @Autowired
    public RouterConfig(RouterHandler routerHandler) {
        this.routerHandler = routerHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> routerHandlerConfig() {
        // 设置 Router 的路径为 /helloWebflux,处理 Handler 为 helloWebflux()
        return RouterFunctions.route(GET("/helloWebflux"),
                routerHandler::helloWebflux);
        // .filter() 添加拦截器，andRoute() 添加更多的路径
    }
}
```

运行项目，使用浏览器访问 :http://localhost:8080/helloWebflux

```json
{"status":"OK","message":"From WebFlux ! From /helloWebflux"}
```

添加一个拦截器，RouterFilter.java

```java
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
```

修改 RouterConfig.java

```java
@Configuration
public class RouterConfig {
    @Resource  RouterFilter filter; // 新增

    public RouterFunction<ServerResponse> routerHandlerConfig() {
        // 在 return 新增 filter(filter)
        return RouterFunctions.route(GET("/helloWebflux"),
                routerHandler::helloWebflux).filter(filter);
    }
}
```

运行项目，使用浏览器访问 :http://localhost:8080/helloWebflux，可以看到返回了 401 状态，字符串 “被拦截了”。

### 2.2、注解式 API

使用 WebFlux 构建 RESTful API 除了可以使用上面 Router Funciton 的方式外，还可以使用 Spring MVC 的 @RestController 相关的注解，新建 WebFluxController.java

```java
@RestController
public class WebFluxController {
    @Resource
    WebFluxService service;

    @GetMapping(value = "/hello")
    public Mono<String> hello() {
        return Mono.just("Hello WebFlux By Controller");
    }
```

跟 Spring MVC 的方式差别并不大，只是将数据流封装到 Mono，Flux 这统一的数据流中。访问 http://localhost:8080/hello 可以看到 *Hello WebFlux By Controller* 的输出。

模拟 数据库查询操作，新建 WebFluxService.java

```java
@Service
public class WebFluxService {
    public Flux<MyMessages> list() {
        MyMessages[] myMessages = new MyMessages[2];
        // TODO 查询数据库，MySQL 等 SQL 数据库暂不支持 Reactive，
        // 操作数据的方式参考 Spring Data JPA 部分，只不过将结果使用 Mono、Flux封装
        myMessages[0] = new MyMessages("ok", "Message 1");
        myMessages[1] = new MyMessages("ok", "Message 2");
        // 多条数据使用 Flux 封装
        return Flux.fromArray(myMessages);
    }
}
```

在 WebFluxController 新增如下函数：

```java
@Resource
WebFluxService service;

@GetMapping("/getList")
public Flux<MyMessages> getList() {
    return service.list();
}
```

重启项目，访问 http://localhost:8080/getList 可以看到如下的输出：

```json
[{"status":"ok","message":"Message 1"},{"status":"ok","message":"Message 2"}]
```

不管是使用 Router Function 还是 Spring MVC 的注解，其产生的效果是相同的，个人偏向于使用注解的方式。不过，实际开发中需要根据实际情况，合理的选择 WebFlux 反应式编程还是传统的方式开发应用，使用 WebFlux 应该使用 Reactive 还是注解。

### 2.2、服务端消息推送

对于以前的应用来说，需要不断更新客户端页面内容适合，需要不断在客户端发送请求，然后使用异步或者同步的方式更新客户端数据。在 HTML 5 之后，引入了两种保持连接的方式，SSE 和 Websocket

 SSE，即为服务端推送（Server Send Event），是客户端发起一次请求后会保持该连接，服务器端会使用该连接持续向客户端发送数据。和 WebSocket 不同的是，SSE 是单向通信的。

这里将简要演示如何使用 WebFlux 开发 SSE，实现服务端不断推动数据到客户端：

WebFluxController.java 新增

```java
@GetMapping("/randomNumbers")
public Flux<ServerSentEvent<Integer>> randomNumbers() {
    // 每次间隔时间 1s
    return Flux.interval(Duration.ofSeconds(1))
    // ThreadLocalRandom.current().nextInt() 产生随机数
            .map(seq -> Tuples.of(seq, ThreadLocalRandom.current().nextInt()))
            .map(data -> ServerSentEvent.<Integer>builder()
                    .event("随机发送信息")
                    .id(Long.toString(data.getT1()))
                    .data(data.getT2())
                    .build()
            );
}
```

访问 http://localhost:8080/randomNumbers ,可以看到输出

```
id:0
event:随机发送信息
data:1339192084

id:1
event:随机发送信息
data:-672769364
```



**小结**

这里简要介绍了 WebFlux 反应式编程，对更加深入的理解可以参考如下资料或者查询 Spring WebFlux 官方文档。

- Router Function 开发 RESTful API
- 使用 MVC 注解的 RESTful API
- SSE 简单示例



**参考和扩展资料：**

https://zhuanlan.zhihu.com/p/45351651

https://juejin.im/post/5b3a24386fb9a024ed75ab36#heading-10

https://www.ibm.com/developerworks/cn/java/spring5-webflux-reactive/index.html