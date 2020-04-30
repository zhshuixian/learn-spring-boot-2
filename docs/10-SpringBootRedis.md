# Spring Boot 2.X 实战--Spring Boot 整合 Redis

> 博客主页：https://me.csdn.net/u010974701
>
> 源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)
>
> 码云：https://gitee.com/ylooq/learn-spring-boot-2

在上一节我们主要实现了 Spring Boot 全局异常处理，这一小节主要是内容为 Spring Boot 整合 Redis ，将实现如下两个内容：

- Spring Boot 直接操作 Redis ，实现 K-V NoSQL 的 set 和 get
- Spring Boot 的数据库缓存

## 1、Redis 的简介和安装

Redis 是互联网应用最为广泛的、最为我们所熟知的 NoSQL 数据库，是存储系统中应用最为广泛的中间件。

> Redis 是一个开源（BSD许可）的，内存存储的数据结构服务器，可用作数据库，高速缓存和消息队列代理等。 它支持多种类型的数据结构，如 字符串（strings）， 散列（hashes）， 列表（lists）， 集合（sets）， 有序集合（sorted sets）。内置复制、Lua 脚本、LRU 收回、事务以及不同级别磁盘持久化功能，同时通过 Redis Sentinel 提供高可用，通过 Redis Cluster 提供自动分区。「参考文献 1」

### 1.1、Redis 使用场景

在微服务和分布式兴起后，Redis 在互联网公司的应用越来越广泛，使用场景也越来越多：

- **缓存**：这是 Redis 使用最多的领域，Redis 将所有的数据直接存在内存中，其访问速度远远快于如 MySQL 等需要从硬盘查询的数据库，如果将 SQL 中常用的数据写入缓存，可以极大的提高系统的性能，减缓数据库的压力。同时也可以通过 Redis 缓存实现跨进程数据的共享；
- **持久化**：例如：对于各个微服务组件来说，自带的 Session 机制并不能跨进程共享，如果将 Session 写入 Redis，既可以实现不同微服务进程的 Session 共享，通过 Redis 持久化到硬盘中，可以避免因服务器宕机重启等导致服务器 Session 丢失；
- **分布式锁**：对于进程间共享的数据，需要通过锁的方式避免脏数据的产生，可以利用 Redis 单线程的特性，实现共享数据的加锁和释放；
- **发布、订阅**：没错，Redis 是可以用作轻量级的消息队列的；
- **更多**：接口访问的限制、缓存用户的行为历史、内容点赞数量、排行榜等

### 1.2、Redis 的安装

这里使用 Linux，对于 Windows 平台，可以使用 WSL 或者虚拟机安装个 Linux 发行版。

```bash
# 安装编译所需的软件
sudo apt install gcc g++ make
```

Redis 官网：https://redis.io/download

```bash
# 下载 Redis 
wget http://download.redis.io/releases/redis-5.0.8.tar.gz
tar -xvf redis-5.0.8.tar.gz
cd redis-5.0.8
# 编译
mark
```

Redis 的简单配置：`vim redis.conf`

```bash
# 添加, Redis 密码，默认为空
requirepass springboot
# 注释掉 bind，允许远程访问，或者空格隔开添加运行远程访问的 ip
# bind 127.0.0.1
# 修改为 no，保护模式关闭，允许通过 IP 访问
protected-mode no
```

Redis 启动

```bash
# 后台以服务的方式运行
src/redis-server redis.conf --daemonize yes
# 使用密码登录默认端口号 6379
src/redis-cli -p 6379 -a springboot
 # 写入 Key = name,value = springboot
127.0.0.1:6379> set name springboot
OK
127.0.0.1:6379> get name
"springboot"
127.0.0.1:6379> del name
(integer) 1
127.0.0.1:6379> get name
(nil)
127.0.0.1:6379>exit
# 关闭 Redis 服务，并把数据保存到硬盘
./src/redis-cli -p 6379 -a springboot  shutdown save
```

## 2、开始使用 Redis

新建项目 *10-spring-boot-redis*，添加如下依赖：

```json
// Gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'org.springframework.boot:spring-boot-starter-web'
testImplementation 'org.springframework.boot:spring-boot-starter-test'
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.1.1'
// 刚开始是 MyBatis，但缓存部分写的有问题，删掉换了 JPA，最后有加上了 MyBatis，所以有两个
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
runtimeOnly 'mysql:mysql-connector-java'
```

文件 `application.properties` 的配置 Redis 连接、MySQL 连接，MyBatis、JPA

```bash
# Redis host ip
spring.redis.host=wsl
# Redis 服务器连接端口
spring.redis.port=6379
# Redis 数据库索引（默认为 0）
spring.redis.database=0
# Redis 服务器连接密码（默认为空）
spring.redis.password=springboot
#连接池最大连接数（使用负值表示没有限制）
spring.redis.jedis.pool.max-active=8
# 连接池最大阻塞等待时间（使用负值表示没有限制）
spring.redis.jedis.pool.max-wait=-1
# 连接池中的最大空闲连接
spring.redis.jedis.pool.max-idle=8
# 连接池中的最小空闲连接
spring.redis.jedis.pool.min-idle=0
# 连接超时时间（毫秒）
spring.redis.timeout=500
# 使用 Redis 缓存
spring.cache.type=redis

# 数据库 URL、用户名、密码、JDBC Driver更换数据库只需更改这些信息即可
# MySQL 8 需要指定 serverTimezone 才能连接成功
spring.datasource.url=jdbc:mysql://localhost:3306/spring?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
spring.datasource.password=xiaoxian
spring.datasource.username=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate 的一些配置
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
# 是否在 Log 显示 SQL 执行语句
spring.jpa.show-sql=true
# hibernate.ddl-auto 配置对数据库表的操作
spring.jpa.hibernate.ddl-auto=none

# MyBatis 驼峰命名转换
mybatis.configuration.map-underscore-to-camel-case=true
# 显示 Mybatis 的 SQL，Mapper 所在的包打印 debug 级别的代码
logging.level.org.xian.redis.repository=debug
```

启动类 `RedisApplication.java` 配置 MyBatis mapper 和 启用缓存配置。

```java
@SpringBootApplication
@EnableCaching
@MapperScan("org.xian.redis.repository")
public class RedisApplication ......
```

### 2.1、Redis 的配置

新建 RedisConfig.java

```java
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisConfig {
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        // 设置缓存过期时间为 120 秒后
        return RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(120)).disableCachingNullValues();
    }
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        // 使用 RedisCacheManager 作为缓存管理器
        return RedisCacheManager.builder(factory).cacheDefaults(cacheConfiguration()).transactionAware().build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // Jackson 序列方式
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        // Jackson 默认自动识别 Public 修饰的成员变量、getter、setter
        // private、protected、public 修饰的成员变量都可以自动识别，无需都指定 getter、setter 或者 public。
        // 参考 https://blog.csdn.net/sdyy321/article/details/40298081
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 对于 8 种基本数据类型及其封装类和 String ，其他的类型在序列化的时候带上该类型和值
        // 参考 https://www.jianshu.com/p/c5fcd2a1ab49
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // Redis 链接
        template.setConnectionFactory(redisConnectionFactory);
        // Redis Key - Value 序列化使用 Jackson
        template.setKeySerializer(jackson2JsonRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // Redis HashKey - HashValue 序列化使用 Jackson
        template.setHashKeySerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
```

默认的 RedisTemplate 为 RedisTemplate<Object, Object>，即 Key 和 Value 都是泛型，将 key 自定义为 String。

使用 Jackson 作为序列化工具，原因是其默认使用 **JdkSerializationRedisSerializer** ，其序列化为二进制存储进 Redis，不利于查看原始内容。使用 JSON 作为序列化工具，其他编程语言也可以方便的访问。

### 2.2、Redis 的 get 和 set

这里将演示通过 RedisTemplate  和 StringRedisTemplate 存储和读取 Redis 的数据。

```java
@RestController
@CacheConfig(cacheNames = "users")
public class RedisController {
    @Resource StringRedisTemplate stringTemplate;
    @Resource RedisTemplate<String, User> redisTemplate;

    @RequestMapping("/setString")
    public String setString(@RequestParam(value = "key") String key,
                            @RequestParam(value = "value") String value) {
        stringTemplate.opsForValue().set(key, value);
        return "ok";
    }
    
    @RequestMapping("/getString")
    public String getString(@RequestParam(value = "key") String key) {
        return stringTemplate.opsForValue().get(key);
    }
    // User 类set、get 和时间限制
    @RequestMapping(value = "/setUser")
    public String setUser(@RequestBody User user) {
        // 1 分钟后过期
        redisTemplate.opsForValue().set(user.getUsername(), user, Duration.ofMinutes(1));
        return "ok";
    }
    
    @RequestMapping("/getUser")
    public User getUser(@RequestParam(value = "key") String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    @RequestMapping("/delUser")
    public User delUser(@RequestParam(value = "key") String key) {
        User user = redisTemplate.opsForValue().get(key);
        // 删除
        redisTemplate.delete(key);
        return user;
    }
}
```

代码解析：set(key, value,time_out)，可以通过第三个参数指定 Redis 的过期时间。

运行项目，分别访问上面的 URL，测试其作用。

### 2.2、数据库缓存

文件 `User.java`, SQL 脚本在 `user.sql`，这里不编写插入 Controller，和省略 service 层。

```java
@Entity
@Table(name = "user", schema = "spring")
public class User implements Serializable {
    private static final long serialVersionUID = 413797298970501130L;
    @Id private String username;
    private Byte age;
    // 省略 Getter setter
}
```

文件 UserMapper：

```java
package org.xian.redis.repository;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.xian.redis.entity.User;

public interface UserMapper {
    @Select("Select username, age From user Where username=#{username}")
    User selectByUsername(String username);
    
    @Update("Update user Set age=#{age} Where username=#{username}")
    void update(User user);

    @Delete("Delete From user where username=#{username}")
    void delete(String username);
}
```

在 `RedisController` 添加如下内容

```java
@RequestMapping("/deleteAllCache")
@CacheEvict(allEntries = true)
public String deleteAllCache() {
    // 删除所有缓存
    return "OK";
}

@RequestMapping("/mySelect")
@Cacheable(key = "#username")
public User mySelect(@RequestParam(value = "username") String username) {
    return userMapper.selectByUsername(username);
}

@RequestMapping("/myUpdate")
@CachePut(key = "#user.username")
public User myUpdate(@RequestBody User user) {
    userMapper.update(user);
    return userMapper.selectByUsername(user.getUsername());
}

@RequestMapping("/myDelete")
@CacheEvict(key = "#username")
public User myDelete(@RequestParam(value = "username") String username) {
    userMapper.delete(username);
    return userMapper.selectByUsername(username);
}
```

代码解析：缓存常用的三个注解。

`@Cacheable(key = "#username")` 表示将 username 作为 key，将结果写入 Redis 缓存。如果缓存中已经有此 key 值，则直接返回，而不会执行 SQL 查询，缓存中没有数据的情况下，才会执行 SQL 查询。即先看缓存有没有，有就直接返回，没有就查询数据库。

`@CachePut(key = "#user.username")`, 跟`@Cacheable` 类似，起作用是在 myUpdate()，执行结束后才会写入或者更新缓存中的内容。

`@CacheEvict(key = "#username")` 根据 key 值删除缓存中的内容。如果指定 allEntries = true 则会删除 `@CacheConfig(cacheNames = "users")` cacheNames  下的所有缓存。

运行项目，访问 /mySelect?username=boot ，第一次访问或者缓存过期后访问，终端会打印 SQL 语句，查询 SQL，缓存有效期内访问，直接返回缓存中的内容。而不会去查数据库。

> 在缓存有效期内访问 /select?username=boot，一个使用 JPA 的 API 接口，同样也是不会查询数据库的。感兴趣的小伙伴可以试试，相关代码已经上传到 GitHub 仓库。
>
> 其他接口这里就不一一列举了。

**踩坑记**：缓存的 @Cache* 并不是用在 Repository 或者 Mapper 接口上的，而应用个具体的方法上。例如在 service 接口服务层或者 Controller 接口层。

另外缓存可以指定 keyGenerator 自动生成 Key 值，这里没有实现，而是通过 key = "#username" 手动指定的方式。



对应 Redis 更多的操作 API ，例如 数组、集合、Hash ，推荐阅读参考文献 1

参考文献 1：了解 Redis 并在 Spring Boot 项目中使用 Redis 「 https://www.ibm.com/developerworks/cn/java/know-redis-and-use-it-in-springboot-projects/index.html 」

下一节的内容为 Spring Boot 整合 MangoDB （另一个常用的 NoSQL）或者简单介绍一下 Redis 的分布式锁。更多内容可以关注小先的 CSDN 博客 『大数据小先』 或者公众号『编程技术进阶』哦。