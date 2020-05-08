# Spring Boot 2.X 实战--Spring Boot 整合 MongoDB

> 源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)
>
> 码云：https://gitee.com/ylooq/learn-spring-boot-2
>
> 博客：https://blog.csdn.net/u010974701

[《Spring Boot 2.X 实战》全文在线阅读 -- 微信平台](https://mp.weixin.qq.com/mp/homepage?__biz=MzIwNDY1NTU1OA==&hid=1&sn=c8584c361e1fcd9b82ecf33217987cf4)

码云 Git Pages https://ylooq.gitee.io/learn-spring-boot-2/#/

在数据量日益暴涨的今天，用户的个人信息如浏览点击记录、商品购买记录等成倍增加，传统的 SQL 数据库已经无法很好的存储和处理这些数据，越来越多的应用使用 NoSQL 存储超大规模的数据集。因为 NoSQL 数据的存储无需固定的格式，因此可以更加方便的扩展。

MongoDB 是一个分布式的文档数据库，具有良好的灵活性，可以方便的进行查询和索引，是提供可扩展的高性能数据存储方案。是所有非关系型数据库中最丰富，最像关系数据库的 NoSQL。

这一小节主要内容是 Spring Boot 整合 MongoDB 和对 MongoDB 进行操作。

## 1、MongoDB 简介和安装

MongoDB  是使用 C++ 编写的开源文档数据库，旨在方便应用开发和数据库的扩展。它将数据存储在类似 JSON 的文档中。

在 MongoDB 中，使用 key-- value 的方法存储数据，其支持数组或者嵌套对象作为值，按照官方的说法，其提供了一种最高效、最自然的数据处理方式。

MongoDB 支持功能强大的查询方式，可以方便地对任何字段进行排序和筛选，查询条件本身就是 JSON 可以方便的编写和动态生成。

最像关系数据库的 NoSQL，支持完整的 ACID 事务。

### 1.1、文档数据库

MongoDB 记录的是一个文档，由 key--value 组成的键值对数据结构，类似于 JSON 对象。

![](https://gitee.com//ylooq/image-repository/raw/master/image2020/20200501181924.png)

- 类似 JSON 的文档对象支持多种编程语言
- 支持嵌入，减少了 SQL 所需的字段连接
- 支持动态扩展

**MongoDB 主要特点**

- 更高的性能
- 丰富的查询语言
- 高可用性
- 水平可扩展

### 1.2、安装 MongoDB

MongoDB 社区版下载连接：https://www.mongodb.com/download-center/community ，选择你系统对应的二进制安装包。

![](https://gitee.com//ylooq/image-repository/raw/master/image2020/20200501183403.png)

特别注意的是，安装过程中不要勾选 “Install MongoDB Compass”，因为这个根本就下载不下来。

![](https://gitee.com//ylooq/image-repository/raw/master/image2020/20200501183627.png)

测试：

```bash
# 服务开启后在终端运行 > mongo
# 使用 test 数据库，没有则创建，show databases 查看已经有的数据库
use test
# 创建一个 集合，类似于 SQL 的一个表，show collections 查看数据库已经有的集合
db.createCollection("springboot");
# 往 springboot 的集合中插入一条数据
db.springboot.insertOne( { name: "Spring Boot With MongoDB" });
# 查找所有 db.getCollection("springboot") 等同于 db.springboot
db.getCollection("springboot").find()
# 根据查询条件查询
db.getCollection("springboot").find({name:"Spring Boot With MongoDB"});
# 删除记录
db.springboot.remove( { name: "Spring Boot With MongoDB" });
# 删除集合
db.springboot.drop()
# 删除当前的数据库
db.dropDatabase()
```

## 2、Spring Boot 整合 MongoDB

新建项目 11-spring-boot-mongo，添加 Spring Web 和 Spring Data MongoDB 这两个依赖。

![](https://gitee.com//ylooq/image-repository/raw/master/image2020/20200501190032.png)

### 2.1、开始使用

文件 application.properties，添加 MongoDB 的数据库 URI

```bash
# 地址、端口号、数据库
spring.data.mongodb.uri=mongodb://127.0.0.1:27017/boot
```

项目启动入口类 Application 添加 @EnableMongoRepositories 装配 MongoDB Repositories

```java
@SpringBootApplication
@EnableMongoRepositories
public class MongoApplication {}
```

新建 User.java 实体类

```java
public class User {
    // 如果不使用 @Id 注解，在插入的时候会自动使用 _id 作为 key 值。
    @Id
    private Integer id;
    private String username;
    private String nickname;
    // 省略 Getter settertoString，
}
```

新建 UserRepository ，语法规则可以参考 JPA，《Spring Boot  2.x 实战--SQL数据库(Spring Data JPA)》：

```java
@Repository
public interface UserRepository extends MongoRepository<User, Integer> {
	// username 包含传入参数的所有文档
    List<User> findByUsernameContains(String username);
    // nickname 包含传入参数的所有文档
    List<User> findByNicknameContaining(String nickname);
    // id 等于传入值的记录，如果有多条，则返回第一条
    User findByIdIs(Integer id);
    // 自定义查询，语法使用 MongoDB 的查询语言，nickname 包含传入参数的所有文档
    @Query("{nickname:{$regex:?0}}}")
    List<User> mySelect(String nickname);
}
```

新建 UserRepositoryTest 测试类，这里使用 Junit 测试的方法，不编写 Controller 和相关的 Service ：

```java
@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserRepositoryTest {
    @Resource MongoTemplate mongoTemplate;
    @Resource UserRepository userRepository;
}
```

MongoDB 的文档操作可以通过继承 MongoRepository 的 JPA 方法，也可以直接使用 MongoTemplate。下面的插入、修改、删除、查询在 UserRepositoryTest  类中添加。

#### 2.1.1、插入一条文档

```java
@Test
public void insert() {
    User user = new User();
    user.setId(1);
    user.setUsername("MongoDB");
    user.setNickname("开源，跨平台，面向文档的数据库");
    userRepository.insert(user);
    // 使用 MongoTemplate
    user.setId(2);
    user.setUsername("Redis");
    user.setNickname("内存中的数据结构存储系统");
    mongoTemplate.insert(user);
}
```

因为在 User.java 声明了 @Id key 值，所以在插入的要手动指定 setId(1) ，不然无法插入会报错。在进行插入、查询等操作的时候，MongoDB 会自己根据 Java 实体类使用那个集合（Collection），类似于 Spring Data JPA 的 @Entity 注解的实体类。

在 Idea 中，使用 Shift+Ctrl+F10 运行该测试方法。

#### 2.1.2、查询

```java
@Test
public void select() {
    System.out.println("查询所有的记录");
    // 方式一
    System.out.println(userRepository.findAll());
    // 方式二
    System.out.println(mongoTemplate.findAll(User.class));
    // 查询某个记录，根据 JPA 方法名命名规则
    System.out.println("根据条件查询 userRepository");
    System.out.println(userRepository.findByUsernameContains("go"));
    System.out.println(userRepository.findByNicknameContaining("内存"));
    // 在 userRepository 自定义查询语句，使用 MongoDB 的查询方式
    System.out.println(userRepository.mySelect("存储系统"));
    // 使用 mongoTemplate
    System.out.println("根据条件查询 mongoTemplate");
    // id = 2
    System.out.println(mongoTemplate.findById(2, User.class));
    // 等同于 SQL 的 where id = 1 and username like %DB%
    Query query = new Query(Criteria.where("id").is(1).and("username").regex("DB"));
    System.out.println(mongoTemplate.find(query, User.class));
}
```

### 2.1.3、修改

```java
@Test
public void update() {
    // 使用 userRepository 更新
    User user = userRepository.findByIdIs(2);
    // 更新 Nickname
    user.setNickname("Redis 开源非关系数据库");
    // save 跟 insert 差不多，不同在于 save 如果存在 ID 值了就更新，还不存在该 ID 值就插入
    userRepository.save(user);
    System.out.println("userRepository 更新数据结果");
    System.out.println(userRepository.findByIdIs(2));

    // 使用 mongoTemplate
    Query query = new Query(Criteria.where("id").is(1));
    Update update = new Update();
    update.set("nickname", "MongoDB 流行的文档数据库 更新：By mongoTemplate");
    mongoTemplate.updateFirst(query, update, User.class);
    System.out.println("mongoTemplate 更新数据结果");
    System.out.println(mongoTemplate.findById(1, User.class));
}
```

### 2.1.4、删除记录

```java
@Test
public void delete() {
    // 使用 userRepository
    userRepository.deleteById(1);
    // 使用 mongoTemplate
    Query query = new Query(Criteria.where("id").is(1));
    // 找到符合条件的然后移除
    mongoTemplate.findAndRemove(query, User.class);
    // 删除所有 user 集合(Collection) 的记录
    userRepository.deleteAll();
    // 删除所有 user 集合(Collection) 的记录
    mongoTemplate.remove(User.class);
}
```

对于 Spring Boot 整合 MongoDB 就到这里，不得不感叹 Spring Boot Starter 的强大，把许多的各种配置自动化，开发人员只需要关注相应的业务逻辑即可。

**踩坑记**

因为安装的 MongoDB 是 4.X 版本，如果 MongoDB 开启了用户和密码验证，使用如下配置连接的时候会报如下错误：

```bash
Exception authenticating MongoCredential{mechanism=SCRAM-SHA-1....
```

找了好多方法，在 Idea 的 DataTools 连接也连接不上。最后发现 Spring Data MongoDB 使用的驱动是 3.X 版本的，可能是因为不兼容的原因，删除了 MongoDB 的密码验证后，可以连接。

```bash
# 附录 MongoDB 开启密码验证
#  mongo 进入 MongoDB 命令行
# 创建/切换到 admin 的数据库
use admin
# 创建管理员 admin
db.createUser({
    user: "admin",
    pwd: "admin123",
    roles: [ { role: "userAdminAnyDatabase", db: "admin" } ]
  })
# 以管理登录
mongo --port 27017 --authenticationDatabase "admin" -u "admin" -p "admin123"
# 方法二
use admin
db.auth("admin","admin123")

# 创建 用户 spring, 对 boot 数据库有读写权限
db.createUser({
    user:"spirng",
    pwd:"springboot",
    roles: [ { role: "readWrite", db: "boot"}]
})
# 退出
exit

# 登录 1
mongo
use boot
db.auth("spirng","springboot")
# 输出 1 表示成功
# 登录方式二  authenticationDatabase 需要授权验证的数据库，即存储用户名和密码的数据库
mongo --port 27017 --authenticationDatabase "admin" -u "spirng" -p "springboot"
```

