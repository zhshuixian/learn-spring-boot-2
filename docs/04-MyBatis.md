# Spring Boot 2.X 实战--SQL 数据库（MyBatis）

>小先博客主页：https://me.csdn.net/u010974701
>
>源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)
>

在上一小节[《实战 SQL 数据库（Spring Data JPA）》](https://mp.weixin.qq.com/s/JfTPcNDAjdn2UKEe5-3rxg)中，主要介绍了 Spring Data JPA 如何连接数据库，实现数据的增删改查等操作。这一小节，将实战 Spring Boot 整合 MyBatis，另一个常用的 Java 持久层框架。

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200308182550.png)

考虑到 MyBatis 应用比较广泛，这里将会使用 MyBatis 作为主要的 Java 持久层框架，对 MyBatis Plus 感兴趣的读者可以对照本小节内容，参考其官网 https://mybatis.plus/ 和其 Spring Boot 启动器 **mybatis-plus-boot-starter**。

这一小节将以用户信息表的为例子实战 MyBatis 连接 SQL 数据库并读写数据，主要分为如下几个部分：

- MyBatis 的依赖引入
- MyBatis 连接 MySQL
- MyBatis 实体类
- MyBatis  写入、更新、删除、查询数据
- MyBatis  多笔查询、分页查询
- MyBatis 使用 Mapper.xml 方式

> 这里使用 MySQL，如果你想使用如：PostgreSQL 等其他的数据库，只需要更改相对应的依赖和指定 Driver 驱动包即可。这里需要你提前安装好 MySQL 或其他 SQL 数据库。
>
> 参考文章在 Linux 下安装 MySQL  8 : https://blog.csdn.net/u010974701/article/details/85625228
>
> 安装完成后运行如下命令：
>
> ```sql
> create database spring;
> ```

## 1、什么是 MyBatis

> MyBatis 官网：https://mybatis.org/mybatis-3/zh/index.html
> MyBatis 是一款优秀的持久层框架，它支持定制化 SQL、存储过程以及高级映射。MyBatis 避免了几乎所有的 JDBC 代码和手动设置参数以及获取结果集。MyBatis 可以使用简单的 XML 或注解来配置和映射原生类型、接口和 Java 的 POJO（Plain Old Java Objects，普通老式 Java 对象）为数据库中的记录。

上一小节讲到，Spring Data JPA 只需要按照规则编写接口的方法名，就可以直接进行相应的 SQL 操作，代码一目了然，开发者几乎不需要跳到 Repository 接口去了解此方法的用途。

相对于 Spring Data JPA 差不多可以不用写 SQL 的框架而言，MyBatis 在于开发者可以灵活的编写 SQL，但带来的麻烦就是项目中一堆 Mapper.xml 等一堆配置文件。即使通过 MyBatis 的代码生成器，自动生成实体类、相关配置文件减少了开发者的工作，但有时更改一个表字段，带来的结果可能是需要同时修改好几个 XML 和 Java 代码，使得开发者在 xml 的配置文件和 Java 代码之间经常切换。    

后来，MyBatis 做了大量的升级优化，可以通过使用注解来减少相关的配置文件。在开篇介绍[《什么是 Spring Boot》](https://mp.weixin.qq.com/s/SbxL2EZBbFc_jiIPm2BnvA)中提到，Spring Boot 一大特色就是**自动配置（AutoConfiguration）**，为许多第三方开发库提供了几乎可以零配置的开箱即用的能力，如 MyBatis。而 MyBatis **开箱即用**的启动器(Starter)  即 **mybatis-spring-boot-starter**，使得 Spring Boot 整合 MyBatis，可以做到几乎 0 配置开发。**mybatis-spring-boot-starter** 支持传统的 Mapper.xml 的配置方法；**支持几乎没有配置的注解方式**。本小节主要使用注解的方式，Mapper.xml 会在文末稍微提一提。

> MyBatis integration with Spring Boot 官方 GitHub 仓库 https://github.com/mybatis/spring-boot-starter
> MyBatis Spring-Boot-Starter will help you use MyBatis with Spring Boot。

## 2、MyBatis 的配置

新建项目 03-sql-mybatis，记得勾选 MyBatis 、MySQL 依赖，**注意 Spring Boot 的版本要为 2.1.X 版本**。

> MyBatis 对 Spring Boot 版本支持
>
> - master(2.1.x) : MyBatis 3.5+, MyBatis-Spring 2.0+(2.0.3+ recommended), Java 8+ and Spring Boot 2.1+
> - 2.0.x : MyBatis 3.5+, MyBatis-Spring 2.0+, Java 8+ and Spring Boot 2.0/2.1.
> - 1.3.x : MyBatis 3.4+, MyBatis-Spring 1.3+, Java 6+ and Spring Boot 1.5

![新建项目](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200308163320.png)

### 2.1、分页插件 Pagehelper

对于 MyBatis 的分页，在本小节中，通过 **Pagehelper** 来实现。

**Gradle 依赖**

```json
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.1.1'
    implementation 'com.github.pagehelper:pagehelper-spring-boot-starter:1.2.13'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'mysql:mysql-connector-java'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation('org.springframework.boot:spring-boot-starter-test') {
        exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
    }
}
```

**Maven 依赖**

```xml
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.mybatis.spring.boot</groupId>
      <artifactId>mybatis-spring-boot-starter</artifactId>
      <version>2.1.1</version>
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
    <!-- https://mvnrepository.com/artifact/com.github.pagehelper/pagehelper-spring-boot-starter -->
    <dependency>
      <groupId>com.github.pagehelper</groupId>
      <artifactId>pagehelper-spring-boot-starter</artifactId>
      <version>1.2.13</version>
    </dependency>
  </dependencies>
```

### 2.2、MySQL、MyBatis、Pagehelper 配置

编辑 /src/main/resources/application.properties 文件，写入如下内容，对于使用注解的方式来说，除此之外没有其他配置了，至于 MyBatis 如何连接到数据库，如何管理连接，Mapper 类如何跟表映射，这些统统交给 **mybatis-spring-boot-starter**：

```bash
# 数据库 URL、用户名、密码、JDBC Driver更换数据库只需更改这些信息即可
# MySQL 8 需要指定 serverTimezone 才能连接成功
spring.datasource.url=jdbc:mysql://localhost:3306/spring?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
spring.datasource.password=xiaoxian
spring.datasource.username=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# MyBatis 驼峰命名转换
mybatis.configuration.map-underscore-to-camel-case=true
# 指定 Mapper 文件的地址
mybatis.mapper-locations=classpath:mapper/*.xml
## pagehelper
pagehelper.helperDialect=mysql
pagehelper.reasonable=true
pagehelper.supportMethodsArguments=true
pagehelper.params=count=countSql
```

在 Spring Boot 启动类 **\*Application** 中，添加 @MapperScan("org.xian.boot.mapper")，自动扫描 org.xian.boot.mapper 下的 Mapper 类，使用此注解，无需再所有的 Mapper 类里添加 @Mapper 注解

```java
@SpringBootApplication
@MapperScan("org.xian.boot.mapper")
public class BootApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }
}
```

### 2.3、通用消息类 MyResponse

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



##  3、开始使用 MyBatis

项目依旧划分为三层，只不过数据持久层框架换成了 MyBatis。

<img src="https://gitee.com/ylooq/image-repository/raw/master/image2020/20200308195133.png" alt="项目结构" style="zoom:50%;" />

**API 接口层**：提供 RESTful API 接口，是系统对外的交互的接口。
**接口服务层**：应用的主要逻辑部分，不推荐在 API 接口层写应用逻辑。
**数据持久层**：编写相应的 MyBatis Mapper 接口，实现与 MySQL 数据库的交互。

本小节中，将实现如下几个 RESTful API 接口：

- /api/user/insert : 插入一条数据
- /api/user/select : 查询一条数据
- /api/user/update : 更新一条数据
- /api/user/delete : 删除一条数据
- /api/user/selectAll : 浏览所有数据
- /api/user/selectPage : 分页浏览

### 3.1、数据表结构和 Mapper 实体类

跟上一小节的表结构一样：

| 字段名   | 字段类型    | 备注             |
| -------- | ----------- | ---------------- |
| user_id  | bigint      | 主键，自增       |
| username | varchar(18) | 用户名，非空唯一 |
| nickname | varchar(36) | 用户昵称，非空   |
| user_age | tinyint     | 用户年龄         |
| user_sex | varchar(2)  | 用户性别         |

**SQL 语句**

```SQL
-- MySQL
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

 **Mapper 实体类**：新建 package，名称为 entity 。在 entity 下新建一个 SysUser 类：

```java
@Data
public class SysUser implements Serializable {
    private static final long serialVersionUID = 4522943071576672084L;

    private Long userId;

    @NotEmpty(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{3,16}$", message = "用户名需3到16位的英文,数字")
    private String username;

    @NotEmpty(message = "用户昵称不能为空")
    private String nickname;

    @Range(min=0, max=100,message = "年龄需要在 0 到 100 之间")
    private Integer userAge;

    private String userSex;
}
```

**@NotEmpty(message = " ")**：不能为空，message 表示 null 或者字符长度为 0 时候的提示信息。

**@Pattern**：正则表达式，例如你可以用来验证用户名、密码是否符合规范。

 **@Range**：指定最大值和最小值，例如指定分数最大是 100。

### 3.2、注解方式的 Mapper 接口

新建 mapper 的包，新建 SysUserMapper 接口。跟 JPA 不同是，你需要通过注解或者 mapper.xml 的方式自定义编写 SQL。**OnXml** 结尾的方法是使用 Mapper.xml 方式指定 SQL ，这在后面会提到，其和不带 **OnXml** 后缀的方法是等同的。

```Java
public interface SysUserMapper {
    /** 往 sys_user 插入一条记录
     * @param sysUser 用户信息
     */
    @Insert("Insert Into sys_user(username, nickname, user_age, user_sex) " +
            "Values(#{username}, #{nickname}, #{userAge}, #{userSex})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insert(SysUser sysUser);
    void insertOnXml(SysUser sysUser);

    /** 根据用户 ID 查询用户信息
     * @param userId 用户 ID
     * @return 用户信息
     */
    @Select("Select user_id,username, nickname, user_age, user_sex From sys_user Where user_id=#{userId}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "userAge", column = "user_age"),
            @Result(property = "userSex", column = "user_sex")
    })
    SysUser selectByUserId(Long userId);
    SysUser selectByUserIdOnXml(Long userId);

    /** 根据用户名更新用户昵称、用户年龄、用户性别 信息
     * @param sysUser 用户信息
     */
    @Update("Update sys_user Set nickname=#{nickname}, user_age=#{userAge}, user_sex=#{userSex} Where username=#{username}")
    void update(SysUser sysUser);
    void updateOnXml(SysUser sysUser);

    /** 根据用户 ID 删除用户信息
     * @param userId 用户 ID
     */
    @Delete("Delete From sys_user where user_id=#{userId}")
    void delete(Long userId);
    void deleteOnXml(Long userId);

    /** 浏览所有用户信息
     * @return 所有用户信息
     */
    @Select("Select * From sys_user")
    List<SysUser> selectAll();
    List<SysUser> selectAllOnXml();
}
```

代码解析：

@Insert，@Select，@Update, @Delete 分别注解 SQL 的 insert，select，update，delete 语句。

MyBatis 的 SQL 传入参数通过 #{param} 的方式，param 的名字应当和你 Java 变量名一样。SQL 的其他部分和标准的 SQL 语句没有区别。

如果 Mapper 接口方法传入的是一个类，也无需手动使用 Getter 方法给 SQL 的传入参数赋值。MyBatis 会自动根据类中的成员变量名自动赋值。

@Results 如果没有开启 MyBatis 驼峰命名转换，或者某些字段不符合驼峰命名转换规则，如：数据库中字段名称为 user_sex，而 Java 类中的成员变量却是 sex,则需要通过此方式手动进行映射。

 @Result(property = "userId", column = "user_id"),property  指定 Java 类的成员变量名，column  指定数据库的字段名。

### 3.3、接口服务层 Service

新增 service 的 Package 包，新增 SysUserService：

```java
@Service
public class SysUserService {
    @Resource
    private SysUserMapper sysUserMapper;

    /** 保存一条记录
     * @param sysUser 用户信息
     * @return 保存结果
     */
    public MyResponse insert(SysUser sysUser) {
        try {
            sysUserMapper.insert(sysUser);
            return new MyResponse("success", "新增成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }

    /** 根据用户 ID 查询一条记录
     * @param userId 用户 ID
     * @return 用户信息
     */
    public SysUser select(Long userId) {
        return sysUserMapper.selectByUserIdOnXml(userId);
    }

    /** 根据用户名更新用户年龄、性别、昵称信息
     * @param sysUser 用户信息
     * @return 结果
     */
    public MyResponse update(SysUser sysUser) {
        try {
            sysUserMapper.update(sysUser);
            return new MyResponse("success", "更新成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }

    /** 根据用户 ID 删除用户信息
     * @param userId 用户 ID
     * @return 操作结果
     */
    public MyResponse delete(Long userId) {
        try {
            sysUserMapper.delete(userId);
            return new MyResponse("success", "删除成功");
        } catch (Exception e) {
            return new MyResponse("error", e.getMessage());
        }
    }

    /** 浏览所有用户信息
     * @return 所有用户信息
     */
    public List<SysUser> selectAll() {
        return sysUserMapper.selectAll();
    }

     /** 分页浏览
     * @return 一页的用户信息
     */
    public PageInfo<SysUser> selectPage(int page,int size) {
        // PageHelper 随后执行的查询会自动分页
        PageHelper.startPage(page, size);
        PageHelper.orderBy("user_id DESC");
        return PageInfo.of(sysUserMapper.selectAllOnXml());
    }
}
```

代码解析：

**@Service**：定义一个 Bean ，此注解的类会自动注册到 Spring 容器。

 **@Resource**：相对于 @Autowired 注解，Bean 的自动装配。

**PageHelper.startPage(page, size);** 调用此方法后，随后的查询讲自动使用分页模式。

**PageInfo.of(sysUserMapper.selectAllOnXml())；** 讲查询返回某一页的信息使用 PageInfo 打包。

### 3.4、 API 接口层

新增 controller 的 Package 包，新增 SysUserController：

```Java 
@RestController
@RequestMapping(value = "/api/user")
public class SysUserController {
    @Resource
    private SysUserService sysUserService;

    @PostMapping(value = "/insert")
    public MyResponse insert(@RequestBody SysUser sysUser) {
        return sysUserService.insert(sysUser);
    }

    @PostMapping(value = "select")
    public SysUser select(@RequestBody Long userId) {
        return sysUserService.select(userId);
    }

    @PostMapping(value = "/update")
    public MyResponse update(@RequestBody SysUser sysUser) {
        return sysUserService.update(sysUser);
    }

    @PostMapping(value = "delete")
    public MyResponse delete(@RequestBody Long userId) {
        return sysUserService.delete(userId);
    }

    @GetMapping("selectAll")
    public List<SysUser> selectAll() {
        return sysUserService.selectAll();
    }

    @GetMapping("selectPage")
    public PageInfo<SysUser> selectPage(@RequestParam(defaultValue = "0") Integer page,
                                        @RequestParam(defaultValue = "3") Integer size) {
        return sysUserService.selectPage(page, size);
    }
}
```

### 3.5、查看运行效果

运行项目，使用 Postman 访问 RESTful API 接口：

- /api/user/insert : 插入一条数据
- /api/user/select : 查询一条数据
- /api/user/update : 更新一条数据
- /api/user/delete : 删除一条数据
- /api/user/selectAll : 浏览所有数据
- /api/user/selectPage : 分页浏览

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200309225308.png)

### 3.6、使用 Mapper.xml 方式自定义 SQL 语句

上文通过注解的方式指定 Mapper 接口的方式，而 Mapper.xml 自定义 SQL 的方式在 Spring Boot 中也做了极大的优化，你只需要在 resources/application.properties 指定 Mapper.xml 文件的位置，其他的配置将由 MyBatis 的启动器(Starter)自动完成。

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200309225717.png)

在 3.2 小节 **Mapper 接口**，每个使用注解自定义 SQL 的方法下面，有个 **OnXml** 后缀的方法，如果在 SysUserService 直接调用此方法会报错。在使用这些方法前，我们还需要在 Mapper.xml 文件下自定义 SQL 字段：

新建 resources/mapper/SysUserMapper.xml 文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!-- 指定 Mapper 类 -->
<mapper namespace="org.xian.boot.mapper.SysUserMapper">
  <!-- 返回结果的字段名、字段类型，对应类的成员变量名 ，可以定义多个 id 值不同的 resultMap -->
  <resultMap id="BaseResultMap" type="org.xian.boot.entity.SysUser">
    <id column="user_id" jdbcType="BIGINT" property="userId"/>
    <result column="username" jdbcType="VARCHAR" property="username"/>
    <result column="nickname" jdbcType="VARCHAR" property="nickname"/>
    <result column="user_age" jdbcType="TINYINT" property="userAge"/>
    <result column="user_sex" jdbcType="VARCHAR" property="userSex"/>
  </resultMap>
  <!-- SQL 语句 的通用部分 -->
  <sql id="SysColumn">
    user_id, username, nickname, user_age, user_sex
  </sql>
  <!-- Id 为 Mapper 类的成员变量名 ，resultMap 指定返回接受类 -->
  <select id="selectAllOnXml" resultMap="BaseResultMap">
    select
    <!-- 使用 Include 包含通用的 SQL 部分 -->
    <include refid="SysColumn"/>
    from sys_user
  </select>

  <select id="selectByUserIdOnXml" parameterType="java.lang.Long" resultMap="BaseResultMap">
    select
    <include refid="SysColumn"/>
    from sys_user Where user_id=#{userId}
  </select>
  <!-- parameterType 指定传入参数类型 -->
  <insert id="insertOnXml" parameterType="org.xian.boot.entity.SysUser">
    Insert Into sys_user(username, nickname, user_age, user_sex)
    Values (#{username}, #{nickname}, #{userAge}, #{userSex})
  </insert>
  <!-- 完整代码看源代码仓库 03-sql-mybatis 的 resources/mapper/SysUserMapper.xml -->
</mapper>
```

具体的代码解析已经在源代码中注释，跟注解方式类似，只不过把**自定义 SQL 挪移到了 XML 文件中**。更改 SysUserService，使其调用 **OnXml** 后缀 Mapper 接口方法。重新运行项目，访问对应的 API，看看结果和注解方式有什么不同。

##### 后记

在 Spring Boot 中整合 MyBatis，其启动器  *mybatis-spring-boot-starter* 将许多配置自动完成，不管是使用注解方式还是 Mapper.xml 的方式，都变得非常简洁。

另外，MyBatis Generator 支持自动生成 Mapper 接口、Mapper.xml、实体类，感兴趣的读者可以自行搜索了解。

下一小节中，将**实战 Spring Boot 2.X 整合 RocketMQ**，对于 NoSQL 的部分将稍微延后。



参考和扩展阅读：

https://mybatis.org/mybatis-3/zh/index.html

https://github.com/mybatis/spring-boot-starter







