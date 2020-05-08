# Spring Boot 2.X 实战--实现一个简单的分布式锁

> 源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)
>
> 码云：https://gitee.com/ylooq/learn-spring-boot-2
>
> 博客：https://blog.csdn.net/u010974701

插入在线阅读的

在 Spring Boot 整合 Redis 中提到，对于进程间共享的数据，需要通过锁的方式避免脏数据的产生，可以利用 **Redis 单线程**的特性，实现共享数据的加锁和释放。本章主要介绍如何实现一个简单的分布式的锁。

在开发 Java 应用的时候，多线程需要对共享数据资源的修改可以通过 synchronized 或者 java.util.concurrent 包加锁实现。而对于分布式系统来说，这些线程间的加锁工具对于不同机器上的进程就失效了。

在分布式系统中，不同机器上服务可能同时访问共享数据资源，如果出现多个服务同时写入、读取就会发生不同客户端获取的数据不一致的情况，最终的数据也会出错。例如在秒杀活动中，本来就 100 件商品，最后可能超卖了几十件。

对于这些情况，需要引入 **分布式锁** ，保证同一时刻只有一个服务访问操作共享数据，使用跨 JVM 的互斥机制控制共享资源的访问。分布式锁有多种实现方式，例如基于数据库、Zookeeper、Redis、Memcached、Chubby。

分布式锁应当具备如下条件：

- **互斥性**：保证同一资源在同一时间只能被一个线程访问
- **高可用、高性能**：获取锁与释放锁的高可用与高性能
- **锁失效机制**：防止拿到锁的线程挂了没有释放锁导致死锁
- **非阻塞锁**：没有获得锁直接返回失败，而不是等待直到获得锁
- **可重入性**：锁过期或释放后，其线程可以继续获得锁，而不会发生数据错误

## 1、简单的 Redis 锁实现

> 参考 Spring Boot 整合 Redis ，安装和运行单机版 Redis。

### 1.1、Redis 是如何获得锁和释放锁的

**获取锁：**

Redis 实现简单的分布式锁方式是，利用 Redis 单线程的特性，使用原子性操作的命令 `SET resource-name anystring NX EX max-lock-time` 实现。

```bash
# Redis 命令
SET key value [EX seconds] [PX milliseconds] [NX|XX]
```

SET 参数说明，从 Redis 2.6.12 版本开始：

- 没有 EX、NX、PX、XX 的情况下，如果 key 已经存在，则覆盖 value 值不管其什么类型；如果不存在，则新建一个 key -- value
- ```EX seconds```: 设置过期时间为 seconds 秒，```SET key value EX second``` 效果等同于 ```SETEX key second value```
- `PX millisecond` ：设置键的过期时间为 `millisecond` 毫秒。 `SET key value PX millisecond` 效果等同于 `PSETEX key millisecond value` 。
- `NX` ：只在 key 不存在时，才对键进行设置操作。 `SET key value NX` 效果等同于 `SETNX key value` 。
- `XX` ：只在 key 已经存在时，才对键进行设置操作。
- value 值：需要加入随机的字符串，作为释放锁的唯一口令(Token)，预防持有过期锁的线程误删其他持线程的锁

客户端执行以上的命令：

- 如果服务器返回 `OK` ，那么这个客户端获得锁。
- 如果服务器返回 `NIL` ，那么客户端获取锁失败，可以在稍后再重试。

设置的过期时间到达之后，锁将自动释放。

**释放锁**

对于锁的释放，不要使用 ```DEL``` 命令，这会导致持有过期锁的线程直接删除其他持线程的锁。应当使用 Lua 脚本，只有当传入的 key--value 和 Redis 中的完全相同时候，才可以释放锁。

````bash
# 简单的 lua 命令删除锁示例，通过 EVAL ...script... 1 resource-name token-value 命令来调用
if redis.call("get",KEYS[1]) == ARGV[1]
then
    return redis.call("del",KEYS[1])
else
    return 0
end
````

### 1.2、开始使用 Redis 锁

新建项目 12-redis-lock ，引入如下依赖：

```json
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    // AOP 依赖，注解实现锁的需要
    compile group: 'org.springframework.boot', name: 'spring-boot-starter-aop', version: '2.1.13.RELEASE'
    testImplementation('org.springframework.boot:spring-boot-starter-test') {
        exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
    }
}
```

application.properties 配置 Redis 服务地址端口密码等：

```bash
# Redis host ip
spring.redis.host=wsl
# Redis 服务器连接端口
spring.redis.port=6379
# Redis 数据库索引（默认为 0）
spring.redis.database=0
# Redis 服务器连接密码（默认为空）
spring.redis.password=springboot
# 连接池最大连接数（使用负值表示没有限制）
spring.redis.jedis.pool.max-active=8
# 连接池最大阻塞等待时间（使用负值表示没有限制）
spring.redis.jedis.pool.max-wait=-1
# 连接池中的最大空闲连接
spring.redis.jedis.pool.max-idle=8
# 连接池中的最小空闲连接
spring.redis.jedis.pool.min-idle=0
# 连接超时时间（毫秒）
spring.redis.timeout=10
```

RedisLockUtil.java 实现 Redis 锁的获取和释放：

```java
@Component
public class RedisLockUtil {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    public boolean lock(String key, String value, long timeout, TimeUnit timeUnit) {
    // 加锁，也可以使用 stringRedisTemplate.opsForValue().setIfAbsent(key, value, 15, TimeUnit.SECONDS);
    // Expiration.from(timeout, timeUnit) 过期时间和单位
    // RedisStringCommands.SetOption.SET_IF_ABSENT ,等同于 NX 当 key 不存在时候才可以
        Boolean lockStat = stringRedisTemplate.execute((RedisCallback<Boolean>) connection ->
                connection.set(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8),
                        Expiration.from(timeout, timeUnit), RedisStringCommands.SetOption.SET_IF_ABSENT));
        return lockStat != null && lockStat;
    }

    public boolean unlock(String key, String value) {
        try {
            // 释放锁使用 Lua 脚本，验证传入的 key--value 跟 Redis 中是否一样
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Boolean unLockStat = stringRedisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.eval(script.getBytes(), ReturnType.BOOLEAN, 1,
                            key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8)));
            return unLockStat == null || !unLockStat;
        } catch (Exception e) {
            logger.error("解锁失败 key = {}", key);
            return false;
        }
    }
}
```

测试，新建 LockController.java ：

```java
@RestController
public class LockController {
    @Resource RedisLockUtil redisLockUtil;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/buy")
    public String buy(@RequestParam String goodId) {
        long timeout = 15;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        // UUID 作为 value
        String lockValue = UUID.randomUUID().toString();
        if (redisLockUtil.lock(goodId, lockValue, timeout, timeUnit)) {
            // 业务处理
            logger.info("获得锁，进行业务处理");
            try {
                // 休眠 10 秒钟
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 释放锁
            if (redisLockUtil.unlock(goodId, lockValue)) {
                logger.error("redis分布式锁解锁异常 key 为" + goodId);
            }
            return "购买成功";
        }
        return "请稍后再试";
    }
}
```

运行项目，同时多个客户端访问 http://localhost:8080/buy?goodId=springboot ，观察其不同的返回结果。

## 2、使用注解实现获取锁和释放锁

在上面的示例中，已经将 Redis 获取和释放封装为一个 RedisLockUtil 类，但使用起来依旧麻烦，有许多重复性的代码，可以抽取出来作为公共的前处理和后处理，业务代码只需要专注具体的业务即可，这里将简单演示一下如何通过 Spring AOP 自定义注解实现 Redis 锁的获取和释放。

对于上面获取锁和释放锁的过程，可以抽取出来作为公共的前处理和后处理，业务代码只需要专注具体的业务即可，这里将简单演示一下如何通过 Spring AOP 自定义注解实现 Redis 锁的获取和释放。

### 2.1、AOP 简介

AOP 即 Aspect Oriented Program 面向切面编程，是面向对象编程的一个重要补充，是 Spring 中最重要的功能之一。

通过 AOP 可以将系统共同调用的逻辑和功能封装起来，如 Redis 获取锁和释放锁的功能。减少重复代码，降低不同模块直接的耦合度，方便未来的扩展和可维护性。例如 RedisLockUtil.java 新增了功能，传入的参数值类型和个数反生了变化，没有使用 AOP 的情况下，所有相关的代码都需要改动。

在面向切面编程的思想中，将功能分为了核心业务和周边功能，AOP 专门用于处理系统中分布于各个模块（不同方法）中的交叉关注点的问题，即为周边功能。

- **核心业务**：具体的业务，例如用户登录、数据库操作等
- **周边功能**：如日志、事物管理、安全检查、缓存等

AspectJ 是一个基于 Java 语言的 AOP 框架，提供了强大的 AOP 功能，其他很多 AOP 框架都借鉴或采纳其中的一些思想。

新建 RedisLock.java ,自定义注解：

```java
// 表示可以注解在方法上
@Target({ElementType.METHOD})
// 注解保留时长，运行时保留
@Retention(RetentionPolicy.RUNTIME)
// 自动继承
@Inherited
@Documented
public @interface RedisLock {
    /** 锁的 key 值，必须为非空字符串 */
    @NotNull
    @NotEmpty
    String key();
    /** 锁的 value 值 */
    String value() default "";
    /** 默认锁有效期 默认 15 */
    long expire() default 15;
    /** 锁有效期期的时间单位，默认 秒 */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
```

接下来实现 RedisLockAspect.java ,对使用 @RedisLock 注解的方法添加前处理和后处理：

```java
@Component
@Aspect
public class RedisLockAspect {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private RedisLockUtil redisLockUtil;
    private String lockKey;
    private String lockValue;

    /** 切入点 RedisLock.java，表示使用了 @RedisLock 注解就进行切入 */
    @Pointcut("@annotation(org.xian.lock.RedisLock)")
    public void pointcut() {
    }

    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint joinPoint)  {
        // 获得其 @RedisLock 注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedisLock redisLock = method.getAnnotation(RedisLock.class);
        // 获取注解上 key value 等值
        lockKey = redisLock.key();
        lockValue = redisLock.value();
        if (lockValue.isEmpty()) {
            lockValue = UUID.randomUUID().toString();
        }
        try {
            Boolean isLock = redisLockUtil.lock(lockKey, lockValue, redisLock.expire(), redisLock.timeUnit());
            logger.info("{} 获得锁的结果是 {} ", redisLock.key(), isLock);
            if (!isLock) {
                // 获得锁失败
                logger.debug("获得锁失败 {}", redisLock.key());
                // 可以自定一个异常类和其拦截器，见 Spring Boot 2.X 实战--RESTful API 全局异常处理
                // https://ylooq.gitee.io/learn-spring-boot-2/#/09-ErrorController?id=spring-boot-2x-%e5%ae%9e%e6%88%98-restful-api-%e5%85%a8%e5%b1%80%e5%bc%82%e5%b8%b8%e5%a4%84%e7%90%86
                // 或者 @AfterThrowing:  异常抛出增强，相当于ThrowsAdvice
                throw new RuntimeException("获取锁失败");
            } else {
                try {
                    // 获取锁成功，进行处理
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException("系统异常");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("系统异常");
        }
    }

    @After(value = "pointcut()")
    public void after() {
        // 释放锁
        if (redisLockUtil.unlock(lockKey, lockValue)) {
            logger.error("redis分布式锁解锁异常 key 为 {}", lockKey);
        }
    }
}
```

 LockController.java  添加

```java
@RequestMapping("/buybuybuy")
@RedisLock(key = "lock_key", value = "lock_value")
public String buybuybuy(@RequestParam(value = "goodId") String goodId) {
    try {
        Thread.sleep(10 * 1000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    return "购买成功";
}
```

运行访问 http://localhost:8080/buybuybuy?goodId=springboot。

缺点，无法像下下面所示代码中 Redis 缓存那样传入 key 值，这功能是可以通过  **Spring Expression Language (SpEL)** ，让自定义注解支持 SpEL 表达式解析，这里先不介绍。

```java
// 自动将 delete(@RequestParam(value = "username") String username)的 username赋值给 key
@CacheEvict(key = "#username")
public User delete(@RequestParam(value = "username") String username) {
    User user = select(username);
    userRepository.delete(user);
    return user;
}
```



**小结：**

简单介绍了单机版 Redis 实现分布式锁的获取和释放，对于集群类型的 Redis 并不太适用，对于实际业务开发，可以根据情况使用 Redisson、RedLock 框架。

简单介绍了自定义注解的实现，不足之处是这里没有实现 SpEL 表达式解析功能，和对 Spring AOP 重要概念的介绍比较简单，下方链接是我看到，写的还不错的介绍博文，可以对照着代码和扩展阅读的资料加深理解。



**参考资料和扩展阅读** 

http://doc.redisfans.com/string/set.html

https://www.ibm.com/developerworks/cn/java/j-lo-springaopcglib/index.html

https://cloud.tencent.com/developer/article/1441626

https://segmentfault.com/a/1190000007469968