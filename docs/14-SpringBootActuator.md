# Spring Boot 2.X 实战--Actuator 监控工具

> 源代码仓库：github.com/zhshuixian/learn-spring-boot-2
>
> 博客：blog.csdn.net/u010974701

Spring Boot 作为提高开发效率的框架，集成了许多的附加功能，例如 Spring Boot Actuator 监控工具，可以作为你在生产环境监视和管理的工具。使得开发者可以用个 HTTP 或者 JMX 的方式，审核、收集生产环境中的运行状况等指标。

## 开始使用 Spring Boot Actuator

新建 项目 *14-spring-boot-actuator* ，引入 Actuator 工具的 starter 启动器 **spring-boot-starter-actuator**

> Actuator 会配置一些 RESTful API 接口，其本身是不带安全验证功能的，在正式的项目可以参考前面 Spring Security 或者 Apache Shiro 的文章，添加安全控制的模块。

```json
// Gradle 依赖
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

Maven

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

### 1、Actuator 端点

Actuator 端点允许你通过 RESTful API 接口监控应用程序并与之交互，其主要分为两类：**内置端点和自定义端点**。内置端点提供基本的应用程序运行状况信息；自定义端点允许你自定义在运行期间监控某些指标。

每个端点都可以配置为允许 HTTP 远程访问，当配置为可用时候，端点会映射到 URL（如果没有配置，则使用默认值）。

端点默认使用 GET 方式访问 /actuator/{Id} ，内置端点的 Id 和描述如下所示：

| Id               | 描述                                                         |
| :--------------- | :----------------------------------------------------------- |
| `auditevents`    | 当前应用程序的审核事件信息                                   |
| `beans`          | 显示应用程序中所有 Spring Bean 以及它们的关系                |
| `caches`         | 可用缓存信息 cacheManagers                                   |
| `conditions`     | 自动配置生效条件以及其是否生效、不生效的原因                 |
| `configprops`    | 显示所有配置属性                                             |
| `env`            | 获取全部环境属性                                             |
| `health`         | 显示应用程序运行状况信息                                     |
| `httptrace`      | 显示 HTTP 请求跟踪信息（默认最后 100 个 HTTP 请求）          |
| `info`           | 显示任意应用程序信息                                         |
| `loggers`        | 显示并修改应用程序中记录器的配置                             |
| `metrics`        | 显示当前应用程序的"指标"信息                                 |
| `mappings`       | 显示所有路径 URL，即`@RequestMapping` 注解的 URL             |
| `scheduledtasks` | 显示应用程序的计划任务                                       |
| `sessions`       | 允许从 Spring Session 中检索和删除用户会话（基于 Spring Session 的 Web 应用） |
| `shutdown`       | 让应用程序正常关闭                                           |
| `threaddump`     | 执行线程转储                                                 |

更多端点参考：https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html

### 2、应用配置

application.properties 文件，下面列出常见的端点配置：

```bash
# 暴露全部可用的内置端点
management.endpoints.web.exposure.include=*
# 运行远程关闭应用
management.endpoint.shutdown.enabled=true

# 暴露部分可用的内置端点，端点 ID 用逗号隔开
# management.endpoints.web.exposure.include=info,health,beans,env

# 不暴露某个端点，端点 ID 用逗号隔开
# management.endpoints.web.exposure.exclude=info,health,beans,env

# 基础路径 /actuator/{Id}
# management.endpoints.web.base-path=/actuator

# 允许的 HTTP 请求方式
# management.endpoints.web.cors.allowed-methods=GET,POST

# 允许跨域
# management.endpoints.web.cors.allowed-origins=https://example.com
```

### 3、内置端点的使用

内置端点的使用比较简单，运行项目，访问 /actuator/{Id}  即可，例如要查看所有的 Spring Beans：

访问 ：http://localhost:8080/actuator/beans ，可以看到如下样式的返回。

![](https://gitee.com//ylooq/image-repository/raw/master/image2020/20200522223437.png)



POST 方式访问 http://localhost:8080/actuator/shutdown ，应用将会关闭

```json
{
    "message": "Shutting down, bye..."
}
```

### 4、自定义端点

新建 MyEndpoint.java

```java
@Configuration
// Endpoint 指定端点 Id
@Endpoint(id = "my-endpoint")
public class MyEndpoint {
    @ReadOperation
    public Map<String, Object> endpoint() {
        Map<String, Object> map = new HashMap<>(2);
        // 将需要监控的信息写入 map 然后返回
        map.put("status", "成功");
        map.put("message", "这是自定义的端点");
        return map;
    }
}
```

如果 没有配置 management.endpoints.web.exposure.include=*，则通过如下方式启用自定义端点：

```bash
# 启用自定义端点，my-endpoint 添加如下配置中
management.endpoints.web.exposure.include=my-endpoint
```

关于 Spring Boot 应用监控的功能到此结束，更多相关信息可以查阅参考和扩展阅读连接：



**参考和扩展阅读**：Spring Boot Actuator 官方文档和 API 说明

https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html

https://docs.spring.io/spring-boot/docs/current/actuator-api/html/

