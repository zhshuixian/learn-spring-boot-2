# Spring Boot  2.x 实战--SQL数据库(Spring Data JPA)

>我是小先，一个专注大数据、分布式技术的非斜杠青年，爱Coding，爱阅读、爱摄影，更爱生活！
>
>大数据小先博客主页：https://me.csdn.net/u010974701
>
>源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)
>

上一小节主要介绍了 Spring Boot 整合 Log4j2 与 Slf4j 实现日志打印和输出到文件。在应用开发中，难免要和数据库打交道，在 Java 生态中，常用的开源持久层框架有 MyBatis 、Hibernate 等，这里要说明一点的是，《Spring Boot 2.X》实战的示例项目将主要使用 MyBatis 或者 MyBatis-Plus（MyBatis 是一款优秀的持久层框架，它支持定制化 SQL、存储过程以及高级映射。MyBatis 避免了几乎所有的 JDBC 代码和手动设置参数以及获取结果集）。

这一小节将以用户信息表的为例子实战 Spring Data JPA 连接 SQL 数据库并读写数据，主要分为如下几个部分：

- JPA 的依赖引入
- JPA 链接 MySQL
- JPA 实体 @Entity、@Id
- JPA 写入、更新、删除、查询数据
- JPA 多条记录写入、查询、分页查询
- JPA 自定义 SQL 查询

> 这里使用 MySQL，如果你想使用如：PostgreSQL 等其他的数据库，只需要更改相对应的依赖和指定 Driver 驱动包即可。这里需要你提前安装好 MySQL 或其他 SQL 数据库。
>
> 参考文章在 Linux 下安装 MySQL  8 : https://blog.csdn.net/u010974701/article/details/85625228
>
> 安装完成后运行如下命令：
>
> ```sql
>create database spring;
> ```

## 1、什么是 Spring Data JPA

JPA(Java Persistence API)，中文名称为 Java 持久化 API，从 JDK 5 开始引入，是 ORM 的标准 Java 规范。JPA 主要是为了简化 Java 持久层应用的开发，整合像 Hibernate、TopLink、JDO 等 ORM 框架，并不提供具体实现。

JPA 的一些优点特性：
**标准化** ：标准的 JPA  规范提供的接口 / 类，几乎不用修改代码就可以迁移到其他 JPA 框架。
**简单易用**：使用注解的方式定义 Java 类和关系数据库之间的映射，无需 XML 配置。
**迁移方便**：更改数据库、更换 JPA 框架几乎不用修改代码。
**高级特性**：媲美 JDBC 的查询能力；可以使用面向对象的思维来操作数据库；支持大数据集、事务、并发等容器级事务；

JPA 主要的技术：
**ORM**：使用注解或者 XML 描述对象和数据表的映射关系
**API**： 规范的 JPA 接口、类。
**JPQL**：面向对象的查询语言，避免程序和具体 SQL 紧密耦合。



Spring Data JPA 是 Spring Data 的子集，**默认使用 Hibernate 作为底层 ORM**。官网文档 https://spring.io/projects/spring-data-jpa 是这么介绍 ：

> Spring Data JPA, part of the larger Spring Data family, makes it easy to easily implement JPA based repositories. This module deals with enhanced support for JPA based data access layers. It makes it easier to build Spring-powered applications that use data access technologies.

Spring 对 JPA 的支持非常强大，使得 JPA 的配置更加灵活；将 EntityManager 的创建与销毁、事务管理等代码抽取出来统一管理；实现了部分 EJB 的功能，如容器注入支持。Spring Data JPA  则更进一步，**简化业务代码，我们只需要声明持久层的接口即可**，剩下的则交给框架帮你完成，**其使用规范的方法名，根据规范的方法名确定你需要实现什么样的数据操作逻辑**。
> 扩展阅读 ：根据方法名自动生成 SQL 规则可以参考微笑哥的博客：http://ityouknow.com/springboot/2016/08/20/spring-boot-jpa.html

使用 Spring Data JPA，你**只需要编写 Repository 接口，依照规范命名你的方法即可**。
- Hibernate 开源 ORM（对象/关系映射）框架。
- Spring Data JPA  默认使用 Hibernate 作为底层 ORM ，是 Spring Data 的子集。

## 2、Spring Data JPA 的配置

Spring Data JPA 如何引入依赖和链接 SQL 数据。

### 2.1、依赖引入

在 IDEA 新建一个项目 02-sql-spring-data-jpa，勾选如下的依赖，相关依赖下载慢的话可以更换国内的镜像源：

![New Project](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200224225809.png)

**Gradle 依赖配置**

```json
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'mysql:mysql-connector-java'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation('org.springframework.boot:spring-boot-starter-test') {
        exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
    }
}
```

**Maven 依赖配置**

```xml
<dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
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
  </dependencies>
```

### 2.2、连接 SQL 数据库

编辑 /src/main/resources/application.properties 文件，写入如下内容：

```bash
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
# create和create-drop:每次都删掉 Entity 对应的数据表并重新创建
# update : 根据 Entity 更新数据表结构，不会删除数据表
# none: 默认值，不做任何操作，实际中推荐使用这个
spring.jpa.hibernate.ddl-auto=none
```

特别说明的是，spring.jpa.hibernate.ddl-auto 使用 create 模式可以方便的自动根据 @Entity 注解的类自动生成对应的数据表，但实际开发中不建议使用，不然重新运行项目就是一次**删(pao)库(lu)**。

```bash
# create 模式的一些日志
Hibernate: drop table if exists sys_user
Hibernate: create table sys_user (user_id bigint not null, user_address varchar(128), user_age integer, username varchar(16), primary key (user_id)) engine=InnoDB
```



## 3、开始使用 Spring Data JPA

项目按功能分为如下三层：

<img src="https://gitee.com/ylooq/image-repository/raw/master/image2020/20200301161620.png" alt="项目结构" style="zoom:67%;" />



**API 接口层**：提供 RESTful API 接口，是系统对外的交互的接口。
**接口服务层**：应用的主要逻辑部分，不推荐在 API 接口层写应用逻辑。
**数据持久层**：编写相应的 Repository 接口，实现与 MySQL 数据库的交互。

## 3.1 数据表结构和 @Entity 实体类

这里使用一个用户信息表作为示例，结构如下所示：

| 字段名   | 字段类型    | 备注             |
| -------- | ----------- | ---------------- |
| user_id  | bigint      | 主键，自增       |
| username | varchar(18) | 用户名，非空唯一 |
| nickname | varchar(36) | 用户昵称，非空   |
| user_age | tinyint     | 用户年龄         |
| user_sex | varchar(2)  | 用户性别         |

**SQL**：如果是 create、update 模式，在代码运行的时候自动生成

```SQL
-- MySQL 数据库，其他数据库可能需要自行修改
create table sys_user
(
	user_id bigint auto_increment,
	username varchar(18) not null,
	nickname varchar(36) not null,
	user_age tinyint null,
	user_sex varchar(2) null,
	constraint sys_user_pk
		primary key (user_id)
);
```

 **@Entity 实体类**：新建 package，名称为 entity。在 entity 下新建一个 SysUser 类：

```java
@Entity
@Getter
@Setter
@Table(name = "sys_user",schema = "spring")
public class SysUser {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long  userId;

    @Column(length = 18,unique = true,nullable = false,name = "username",updatable = true)
    @NotEmpty(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{3,16}$", message = "用户名需3到16位的英文,数字")
    private String  username;

    @Column(length = 18,nullable = false)
    @NotEmpty(message = "用户昵称不能为空")
    private String  nickname;

    @Range(min=0, max=100,message = "年龄需要在 0 到 100 之间")
    private Integer userAge;

    // userSex 会自动映射到 user_sex 的字段名
    @Column(length = 2)
    private String  userSex;
}
```

**代码解析**：

**@Entity**：表明这是一个实体类，在 JPA 中用于注解 ORM 映射类。

**@Table(name = "sys_user", schema = "spring")**：注明 ORM 映射类对应的表名，默认是类名。如：SysUser 映射为 sys_user，**Java 驼峰法命名映射到 SQL 时，用下划线 _ 隔开**，字段名也是同样的规则。schema 指定数据库，默认就是数据库连接配置中指定的。

**@Id**：主键注解。

 **@GeneratedValue(strategy=GenerationType.IDENTITY)**：指定主键生成策略。

- IDENTITY，自增主键，由数据库自动生成
- SEQUENCE，序列，根据数据库的序列来生产主键
- TABLE ，指定一个数据表来保存主键
- AUTO，由程序自动控制，默认值

**@Column(length = 18,unique = true,nullable = false,name = " ", updatable = true)**：指定字段的长度、是否唯一、是否可以 null、字段名，是否可以更新这个字段。其中默认非唯一约束、可以为 null 值，字段名默认根据名称规则映射，updateable 默认 true。

**@NotEmpty(message = " ")**：不能为空，message 表示 null 或者字符长度为 0 时候的提示信息。

**@Pattern**：正则表达式，例如你可以用来验证用户名、密码是否符合规范。

 **@Range**：指定最大值和最小值，例如指定分数最大是 100。

### 3.2、编写 JpaRepository 接口

新建 repository 包，新建 SysUserRepository 接口，你并不需要编写其他代码，就已经能够基本对数据库进行 CURD 了：

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xian.boot.entity.SysUser;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
// JpaRepository<SysUser, Long> ,第一个参数指定 Entity 实体类，第二个指定主键类型
}
```

## 3.3、增加、查询、更新、删除

目标，实现对数据库进行增删改查的 RESTful API 接口。

**MyResponse**：通用消息返回类，增加、删除、修改操作是否成功和信息返回的类：

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MyResponse implements Serializable {
    private static final long serialVersionUID = -2L;
    private String status;
    private String message;
}
```

#### 3.3.1、增加一条数据

实现一个新增用户数据的 API 接口。新建 service 包，新建 SysUserService 类：

```java
package org.xian.boot.service;
import org.springframework.stereotype.Service;
import org.xian.boot.MyResponse;
import org.xian.boot.entity.SysUser;
import org.xian.boot.repository.SysUserRepository;
import javax.annotation.Resource;

@Service
public class SysUserService {
    @Resource
    private SysUserRepository sysUserRepository;

    /**
     * 保存一条记录
     * @param sysUser 用户信息
     * @return 保存结果
     */
    public MyResponse save(SysUser sysUser) {
        try {
            sysUserRepository.save(sysUser);
            return new MyResponse("success", "新增成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }
}

```

**代码解析**：

**@Service**：定义一个 Bean ，此注解的类会自动注册到 Spring 容器。

 **@Resource**：相对于 @Autowired 注解，Bean 的自动装配。

在上文 SysUserRepository 接口中，save() 这个方法继承自 CrudRepository，部分源码如下：

```java
package org.springframework.data.repository;
@NoRepositoryBean
public interface CrudRepository<T, ID> extends Repository<T, ID> {
	/**
	 * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
	 * entity instance completely.
	 *
	 * @param entity must not be {@literal null}.
	 * @return the saved entity; will never be {@literal null}.
	 * @throws IllegalArgumentException in case the given {@literal entity} is {@literal null}.
	 */
	<S extends T> S save(S entity);
}
```

新建 RESTful API 接口，新建 controller 包，新建 SysUserController 类：

```java
@RestController
@RequestMapping(value = "/api/user")
public class SysUserController {
    @Resource
    private SysUserService sysUserService;

    @PostMapping(value = "/save")
    public MyResponse save(@RequestBody SysUser sysUser) {
        return sysUserService.save(sysUser);
    }
}
```

运行项目，在 Postman 使用 POST 方式提交如下数据到 http://localhost:8080/api/user/save，改变不同的输入值，验证 @Entity 类中的注解的作用。改变一些值，往数据库多写入几条数据。

```json
{
	"username":"xiaoxian",
	"nickname":"小先哥哥",
	"userAge":17,
	"userSex":"男"
}
```

### 3.3.2、查询一条数据

功能，根据用户名 username 查询用户信息，SysUserRepository 类中新增：

```java
    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser findByUsername(String username);
```

SysUserService 新增：

```java
    public SysUser find(String username) {
        return sysUserRepository.findByUsername(username);
    }
```

SysUserController 新增：

```java
    @PostMapping(value = "/find")
    public SysUser find(@RequestBody String username) {
        return sysUserService.find(username);
    }
```

重新运行，使用 Postname 访问 http://localhost:8080/api/user/find

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200301231942.png)

### 3.3.3、更新用户数据

目标，实现一个根据用户名更改用户信息的接口，SysUserService 新增：

```java
    public MyResponse update(SysUser sysUser) {
        // 实际开发中，需要根据具体业务来写相应的业务逻辑，这里只是做一个示例
        try {
            // 需要先根据用户名 username 查询出主键，然后再使用 save 方法更新
            SysUser oldSysUser = sysUserRepository.findByUsername(sysUser.getUsername());
            sysUser.setUserId(oldSysUser.getUserId());
            sysUserRepository.save(sysUser);
            return new MyResponse("success", "更新成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }
```

SysUserController 新增：

```java
    @PostMapping(value = "/update")
    public MyResponse update(@RequestBody SysUser sysUser){
        return sysUserService.update(sysUser);
    }
```

重新运行，使用 Postname 访问 http://localhost:8080/api/user/update。

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200301233542.png)

### 3.3.4、删除一条数据

目标，实现一个 API 接口，实现对用户信息的删除，SysUserService 新增：

```java
    public MyResponse delete (String username){
        try {
            SysUser oldSysUser = sysUserRepository.findByUsername(username);
            sysUserRepository.delete(oldSysUser);
            return new MyResponse("success", "删除成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }
```

SysUserController 新增：

```java
    @PostMapping(value = "/delete")
    public MyResponse delete(@RequestBody String username){
        return sysUserService.delete(username);
    }
```

重新运行，使用 Postname 访问 http://localhost:8080/api/user/delete

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200302220502.png)

### 3.4 、多条记录写入

多条记录的写入跟单条记录的写入差不多。SysUserService 新增：

```java
    public MyResponse saveAll(List<SysUser> sysUserList) {
        try {
            sysUserRepository.saveAll(sysUserList);
            return new MyResponse("success", "新增成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }
```

SysUserController 新增：

```java
    @PostMapping(value = "/saveAll")
    public MyResponse saveAll(@RequestBody List<SysUser> sysUserList) {
        return sysUserService.saveAll(sysUserList);
    }
```

重新运行，使用 Postname 访问 http://localhost:8080/api/user/saveAll  ，更改传入数据，查看返回结果。

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200302000058.png)

### 3.5、浏览全部记录

实现一个 API 接口，查询所有的记录，SysUserService 新增：

```java
    public List<SysUser> list(){
        return sysUserRepository.findAll();
    }
```

SysUserController 新增：

```java
    @GetMapping(value = "list")
    public List<SysUser> list(){
        return sysUserService.list();
    }
```

重新运行，使用 Postname  GET 方式访问 http://localhost:8080/api/user/list ，可以看到返回数据库表中所有的数据。

### 3.6、分页浏览

在 3.5 小节中，使用此方式查询的是全部数据，对于数据量大的表来说非常不方便，这里将实现一个分页浏览的 API 接口：

SysUserService 新增：

```java
    public Page<SysUser> page(Integer page, Integer size) {
        // 根据 userId 排序，Sort.Direction.ASC/DESC 升序/降序
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "userId");
        return sysUserRepository.findAll(pageable);
    }
```

SysUserController 新增：

```java
    @PostMapping(value = "page")
    public Page<SysUser> page(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "3") Integer size) {
        // page 从 0 开始编号
        // 默认浏览第一页，每页大小为 3
        return sysUserService.page(page, size);
    }
```

重新运行，使用 Postname  方式访问 http://localhost:8080/api/user/page?page=1&size=2 ，可以看到返回如下所示的数据：

```json
{	// content 结果集
    "content": [
        {
            "userId": 12,
            "username": "zhang",
            "nickname": "张小先",
            "userAge": 23,
            "userSex": "男"
        },
        {
            "userId": 16,
            "username": "daxian",
            "nickname": "大先哥哥",
            "userAge": 19,
            "userSex": "男"
        }
    ],
    "pageable": {
        // 排序信息
        "sort": {
            "sorted": true,
            "unsorted": false,
            "empty": false
        },
        "offset": 2,
        "pageSize": 2, // 每页数据集大小
        "pageNumber": 1, // 页数
        "paged": true,
        "unpaged": false
    },
    "totalElements": 5, // 总数据量
    "last": false, // 是否最后一页
    "totalPages": 3, // 总页数
    "size": 2, // 每页数据集大小
    "number": 1, // 当前页数
    "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
    },
    "numberOfElements": 2, // content 内容的数量
    "first": false, // 是否第一页
    "empty": false // content 内容是否为空
}
```

### 3.6、自定义查询 SQL

对于简单查询来说，可以通过**扩展阅读**介绍的规则，直接根据规则编写方法名即可，无需自己额外编写 SQL。在 JPA 中，可以通过 @Query 注解自定义 SQL 查询语句。这里将演示根据用户昵称 Nickname 模糊查询用户信息：

SysUserRepository 类中新增：

```java
    /**
     * 根据用户昵称查询用户信息 等价于 findByNicknameLike
     *
     * @param nickname 用户昵称
     * @param pageable 分页
     * @return 用户信息
     */
    @Query("SELECT sysUser  from SysUser sysUser where sysUser.nickname like %:nickname%")
    Page<SysUser> searchByNickname(@Param("nickname") String nickname, Pageable pageable);

    /**
     * 根据用户昵称查询用户信息 和 searchByNickname 等价
     *
     * @param nickname 用户昵称
     * @param pageable 分页
     * @return 用户信息
     */
    Page<SysUser> findByNicknameLike(@Param("nickname") String nickname, Pageable pageable);

```

SysUserService 新增：

```java
    public Page<SysUser> searchByNickname(String nickname, Integer page, Integer size) {
        // 根据 userId 排序
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "userId");
        return sysUserRepository.searchByNickname(nickname,pageable);
    }
```

SysUserController 新增：

```java
    @PostMapping(value = "search")
    public Page<SysUser> search(@RequestParam String nickname, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "3") Integer size) {
        return sysUserService.searchByNickname(nickname, page, size);
    }
```

重新运行，使用 Postname  方式访问 http://localhost:8080/api/user/search?nickname=瑞&page=0&size=5 ，可以看到返回如下所示的数据：

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200302214330.png)



## 4、本章小结

本章主要介绍 Spring Data JPA 和如何使用，实战演示了 JPA 如何进行数据库的增加、删除、修改、更新操作，对于像 JPA 的多表查询，多数据源支持由于篇幅的原因不再一一展开，感兴趣的读者可以通过参考文档、扩展阅读和搜索引擎进一步深入了解。

在 JPA 中，多表查询有两种方式：

一种是 JPA 的级联查询，**@OneToMany，@ManyToOne**等注解在  **@Entity 实体类** 中指定多表关联规则；

另一种就是新建一个类，用于接收返回的结果集，然后通过 **@Query** 自定义查询 SQL；



**扩展阅读**： http://ityouknow.com/springboot/2016/08/20/spring-boot-jpa.html

**参考链接**：https://www.ibm.com/developerworks/cn/opensource/os-cn-spring-jpa/index.html

https://docs.spring.io/spring-data/jpa/docs/2.2.5.RELEASE/reference/html/



下一章，将实战 Spring Boot 如何集成 MyBatis 或者 MyBatis-Plus，后续的实战演示项目也会以这两个框架之一为主 。

- MyBatis 是一款优秀的持久层框架，它支持定制化 SQL、存储过程以及高级映射。MyBatis 避免了几乎所有的 JDBC 代码和手动设置参数以及获取结果集。
- MyBatis-Plus（简称 MP）是一个 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，为简化开发、提高效率而生。

个人比较倾向于  MyBatis-Plus ，不过实际开发中应该  **MyBatis 使用比较广泛**。如果你有什么好的建议，可以点击下方留言告诉小先。