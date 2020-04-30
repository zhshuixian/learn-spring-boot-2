# Spring Boot 2.X 实战--Spring Security 登录和注册

> 作者：小先
>
> 博客主页：https://me.csdn.net/u010974701
>
> 源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)

对于 Web 系统来说，对页面和 API 接口的访问权限进行安全控制是必须的，例如需要阻止非系统用户的访问，控制不同页面或接口的访问权限。在 Java 开发中，常用的安全框架有 Spring Security 和 Apache Shiro。

Spring Security 是 Spring 生态体系的安全框架，其基于 Spring AOP 和 Servlet 过滤器实现，是 Spring Boot 推荐使用的安全框架。它提供全面的安全性解决方案，同时在 Web 请求级和方法调用级处理身份确认和授权。

Spring Security 主要包括如下两个部分：

- 登录认证（Authentication）
- 访问授权（Authorization）

本小节中，将实战 Spring Boot 整合 Spring Security，**实现用户的注册、登录和角色权限的访问控制**。

数据库使用  MySQL，数据持久层框架使用 MyBatis。

Spring Security 默认使用 Session，因此不使用 RESTful API ，使用 MVC 模式。Thymeleaf 作为 Web 页面的模板引擎

## 1）依赖引入和项目配置

新建项目 05-spring-security，**注意 Spring Boot 的版本要为 2.1.X 版本**

### 1.1）依赖引入

Spring Security 为 Spring Boot 提供的 Starter (启动器) ，使得 Spring Boot 整合 Security 几乎可以做到 0 配置开发。

thymeleaf-extras-springsecurity5 是 Thymeleaf  的扩展，实现在 Web 页面控制 Web 元素的展示。

**Gradle 项目依赖**

```json
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.1.1'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    runtimeOnly 'mysql:mysql-connector-java'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    // https://mvnrepository.com/artifact/org.thymeleaf.extras/thymeleaf-extras-springsecurity5
    compile group: 'org.thymeleaf.extras', name: 'thymeleaf-extras-springsecurity5', version: '3.0.4.RELEASE'
```

Maven 项目依赖部分见文章最低部

### 1.2）项目配置

配置 MySQL 数据库和 MyBatis 驼峰命名转换，application.properties

```bash
# 数据库 URL、用户名、密码、JDBC Driver更换数据库只需更改这些信息即可
# MySQL 8 需要指定 serverTimezone 才能连接成功
spring.datasource.url=jdbc:mysql://localhost:3306/spring?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
spring.datasource.password=xiaoxian
spring.datasource.username=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# MyBatis 驼峰命名转换
mybatis.configuration.map-underscore-to-camel-case=true
```

添加 @MapperScan

```java
@MapperScan("org.xian.security.mapper")
public class SecurityApplication {}
```



## 2）开始使用 Spring Security

项目使用类似 MVC 的三层模型

<img src="https://gitee.com/ylooq/image-repository/raw/master/image2020/20200325231259.png" alt="image-20200325231257474" style="zoom: 45%;" />

**View 展示层**：Thymeleaf  渲染的 Web 页面。
**Controller 控制器**：应用的主要逻辑部分。
**Model 模型层**：编写相应的 MyBatis Mapper 接口，实现与 MySQL 数据库的交互。

### 2.1）数据表结构和 Mapper 实体类

新建如下 用户表 sys_user


| 字段    | 类型      | 备注           |
| --------- | ----------- | ---------------- |
| user_id   | bigint      | 自增主键     |
| username  | varchar(18) | 用户名，非空唯一 |
| password  | varchar(60) | 密码，非空  |
| user_role | varchar(8)  | 用户角色(USER / ADMIN) |

这里用户角色有 USER / ADMIN ，对于一个用户可能有多个角色的情况暂不考虑。

SQL

```
use spring;
create table sys_user
(
    user_id   bigint auto_increment,
    username  varchar(18)  not null unique,
    password  varchar(60) not null,
    user_role varchar(8)   not null,
    constraint sys_user_pk
        primary key (user_id)
);
```

 **Mapper 实体类**：新建 package，名称为 entity 。在 entity 下新建一个 SysUser 类：

```java
public class SysUser implements Serializable {
    private static final long serialVersionUID = 4522943071576672084L;
    private Long userId;
    private String username;
    private String password;
    private String userRole;
    // 省略 getter setter constructor
}
```

## 2.2）Mapper 接口

```java
// 这里使用注解的方式
public interface SysUserMapper {
    /** 往 sys_user 插入一条记录
     * @param sysUser 用户信息
     */
    @Insert("Insert Into sys_user(username, password,user_role) Values(#{username}, #{password},#{userRole})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insert(SysUser sysUser);

    /** 根据用户 Username 查询用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @Select("Select user_id,username, password,user_role From sys_user Where username=#{username}")
    SysUser selectByUsername(String username);
}
```

## 2.3）Spring Security 配置

使用 Spring Security ，只需要实现 UserDetailsService 接口和继承 WebSecurityConfigurerAdapter 。

新建 security 的包，新建 MyUserDetailsServiceImpl、SpringSecurityConfig 类

**实现 UserDetailsService** 

```java
@Service
public class MyUserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (null == sysUser) {
            throw new UsernameNotFoundException(username);
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + sysUser.getUserRole()));
        return new User(sysUser.getUsername(), sysUser.getPassword(), authorities);
    }
}
```

**代码解析：**

*loadUserByUsername*：通过重写 UserDetailsService 接口的 loadUserByUsername 方法，给 Spring Security 传入用户名、用户密码、用户角色。

*List\<SimpleGrantedAuthority\>* ：authorities.add 可以增加多个用户角色，对于一个用户有多种角色的系统来说，可以通过增加用户角色表、用户--角色映射表，存储多个用户角色信息。

*"ROLE_" + sysUser.getUserRole()* ：Spring Security 角色名称默认使用 "ROLE_" 开头。

这里查询用户信息的时候并没有验证用户密码，是因为密码验证部分通过 Spring Security 来完成。



**SpringSecurityConfig 类**

```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    @Resource
    private MyUserDetailsServiceImpl userDetailsService;

    @Override
    public void configure(WebSecurity web) {
        // 忽略前端静态资源 css js 等
        web.ignoring().antMatchers("/css/**");
        web.ignoring().antMatchers("/js/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 设置密码加密方式，验证密码的在这里
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 BCryptPasswordEncoder
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 允许无授权访问 "/login"、"/register" "/register-save"
        // 其他地址的访问均需验证权限
        http.authorizeRequests()
                .antMatchers("/login", "/register", "/register-save", "/error").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                // 用户名和用户密码参数名称
                .passwordParameter("password") 
                .usernameParameter("username")
                // 指定登录页面
                .loginPage("/login")
                // 登录错误跳转到 /login-error
                .failureUrl("/login-error")
                .permitAll()
                .and()
                // 设置退出登录的 URL 和退出成功后跳转页面
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login");
    }
}
```

**代码解析**：

*BCryptPasswordEncoder* ：通过 BCrypt 强哈希方法加密存储密码。对于 Web 系统来说，几乎不会明文存储密码。Spring Security 提供的 BCryptPasswordEncoder 类实现密码加密的功能。BCrypt 强哈希方法每次加密的结果是不同的。

### 2.3）实现用户注册功能

在 resources/templates/ 中，新建 register.html 

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>注册</title>
</head>
<body>
<p>注册页面</p>
<p th:if="${error}" class="error">注册错误</p>
<form th:action="@{/register-save}" method="post">
    <label for="username">用户名：</label>:
    <input type="text" id="username" name="username" autofocus="autofocus"/> <br/>
    <label for="password">密码：</label>:
    <input type="password" id="password" name="password"/> <br/>
    <label for="userRole">用户角色</label>:
    <select name="userRole" id="userRole">
        <option value="ADMIN">管理员</option>
        <option value="USER">普通用户</option>
    </select>
    <br/>
    <input type="submit" value="注册"/><br/>
    <a href="index.html" th:href="@{/}">返回首页</a> <br/>
    <a href="login.html" th:href="@{/login}">登录</a>
</form>
</body>
</html>
```

代码解析：

th: 开头的标签表示由 Thymeleaf 渲染。th:if 表示判断，th:action URL 提交路径，更多 Thymeleaf  功能可以通过官网了解 https://www.thymeleaf.org/



新建 controller 包，在其下面新建 RegisterController 类：

```java
@Controller
public class RegisterController {
    @Resource
    private SysUserMapper sysUserMapper;

    @RequestMapping("/register")
    public String register() {
        return "register";
    }

    @RequestMapping("/register-error")
    public String registerError(Model model) {
        // Model 的作用是往 Web 页面穿数据
        // model 添加一个参数 error 其作用是如果此参数为 true，就显示下面一行 HTML 代码
        // <p th:if="${error}" class="error">注册错误</p>
        model.addAttribute("error", true);
        return "register";
    }

    @RequestMapping("/register-save")
    public String registerSave(@ModelAttribute SysUser sysUser,
                               Model model) {
        // 判断 username password 不能为空
        if (sysUser.getUsername() == null || sysUser.getPassword() == null || sysUser.getUserRole() == null) {
            model.addAttribute("error", true);
            return "register";
        }
        try {
            // 密码加密存储
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String password = bCryptPasswordEncoder.encode(sysUser.getPassword());
            sysUser.setPassword(password);
            // 写入数据库
            sysUserMapper.insert(sysUser);
            //  重定向到 login 页面
            return "redirect:/login";
        } catch (Exception e) {
            // 注册错误
            model.addAttribute("error", true);
            return "register";
        }
    }
}
```

**代码解析**：

*@Controller* 注解表示返回一个 HTML 页面。return "register" 表示返回 register.html ；return "redirect:/xxx" 重定向到某个页面。

*@ModelAttribute SysUser sysUser* ：类似 @RequestBody，从表单中读取数据，赋值给 SysUser。

运行项目，通过浏览器访问 http://localhost:8080/register ，输入用户名、密码，选择用户角色，点击注册。

> 记得添加一下 -Djava.security.egd=file:/dev/./urandom 这个 JVM 运行参数，好像是 Security 的 随机数 方面的 bug。
>
> ![image-20200326002456189](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200326002458.png)

### 2.4）实现用户登录功能

新建 login.html ，和 index.html（在 2.6 小节）。

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>登录</title>
</head>
<body>
<p>登录</p>
<p th:if="${error}" class="error">用户名或密码错误</p>
<form th:action="@{/login}" method="post">
    <label for="username">用户名</label>:
    <input type="text" id="username" name="username" autofocus="autofocus"/> <br/>
    <label for="password">用户密码</label>:
    <input type="password" id="password" name="password"/> <br/>
    <input type="submit" value="登录"/>
</form>
</body>
</html>
```

在 controller 包下新建 LoginController 类：

```java
@Controller
public class LoginController {

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/login-error")
    public String loginError(Model model) {
        // 登录错误
        model.addAttribute("error", true);
        return "login";
    }
}
```

因为我们已经指定 /login 和 /login-error 这两个路径的作用，因此登录和登录失败的具体实现由 Spring Security 实现。

运行项目，通过浏览器访问 http://localhost:8080/login ，输入用户名、密码，选择用户角色，点击登录。

### 2.5）用户角色权限控制

用户角色权限控制，可以通过 @PreAuthorize 注解在 @RequestMapping 上，表示这个 URL 需要某种角色权限才能访问；还有一种是通过 Thymeleaf 实现页面某些元素需要指定角色权限才行访问。

这里先介绍第一种方式 @PreAuthorize 注解：

在 controller 包下新建 AdminController ，对应的 admin.html 可以查看仓库源码：

```java
@Controller
public class AdminController {
    // 需要 ROLE_ADMIN 角色才行访问 /admin
    // 这也是为什么 MyUserDetailsServiceImpl 需要 "ROLE_" + sysUser.getUserRole()
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/admin")
    public String admin() {
        return "admin";
    }
}
```

重新运行项目，使用不同角色 USER、ADMIN 登录，访问 http://localhost:8080/admin。查看不同角色权限的运行结果。

### 2.6）Thymeleaf 角色控制

对于同一个页面，可能部分元素是 ADMIN 可见的，部分是 USER 可见的。这些通过 Thymeleaf  的扩展插件 thymeleaf-extras-springsecurity 实现。

修改 index.html 页面：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="UTF-8">
    <title>主页</title>
</head>
<body>
<h1>登录成功</h1>
<div sec:authorize="isAuthenticated()">
    登录的用户都可以见
</div>
<br/>
<div sec:authorize="hasRole('ROLE_ADMIN')">
    管理员才可以看见 <br/>
    <a href="admin.html" th:href="@{/admin}">管理页</a>
</div>
<br/>
<div sec:authorize="hasRole('ROLE_USER')">
    用户才可以看见
</div>
<br/>
<br/>
<form th:action="@{/logout}" method="post">
    <input type="submit" value="退出登录"/>
</form>
</body>
</html>
```



## 附录

**Maven 项目依赖**

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.1.2</version>
  </dependency>
  <dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
  </dependency>
  <!-- https://mvnrepository.com/artifact/org.thymeleaf.extras/thymeleaf-extras-springsecurity5 -->
  <dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity5</artifactId>
    <version>3.0.4.RELEASE</version>
  </dependency>
</dependencies>
```



本小节主要实战了 Spring Boot 整合 Spring Security，实现用户的注册、登录和角色控制。下一小节，将实战 Spring Boot 整合 JJWT 实现 Token 认证和授权的 RESTful API 接口。