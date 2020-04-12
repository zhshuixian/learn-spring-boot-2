# Spring Boot 2.X 实战--Spring Security (Token)登录和注册

> 博客主页：https://me.csdn.net/u010974701
>
> 源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)

在上一节《Spring Boot 2.X 实战--Spring Security 登录和注册》中，我们主要整合 Spring Security，实现了用户的注册、登录和权限控制。

在这一节中，我们将实现基于 Token 令牌的 Web 应用安全访问控制。Token 令牌是一种无状态的认证方式，它不会像 Session 那样在服务器端保留用户认证方面的信息，非常适合 RESTful API。

对于纯 Token 认证的 Web 应用，大概思路是：在上一个项目基础上，关闭 Spring Security 自带的 Session，允许跨域请求；增加一个 Token 拦截器，拦截所有请求并验证 Token 令牌是否有效。

如果想要实现 Session + Token，我的想法是，增加 Session、Token 拦截器，同样拦截所有的请求。带 Cookie 的请求用 Session 认证授权；不带 Cookie 的交给 Token 拦截器认证验证授权。（这个想法暂没有实践过，嘻嘻，可以的话欢迎留言告诉小先）。

本小节中，不使用 MVC 模式，使用 MySQL，MyBatis 构建 RESTful API 实现。

## 1）什么是 Token

### 1.1）服务器是怎么知道用户有没有登录的

我们知道，HTTP 协议本身是不包括状态的，每一次客户端——服务器 HTTP 通信结束后就断开了，因此 HTTP 不会记住这个登录用户是谁。

为了解决每一次请求都要带上用户名、用户密码来进行用户身份验证的情况，一般有以下两种方法，两种方法各有优劣，根据项目实际情况选用即可。

**基于 Session 的认证方法**

1、用户登录后，在服务端生成用户的登录信息（Session），并把登录信息的 ID（Session_ID）返回给客户端。客户端一般通过 Cookie 保存。

2、客户端每一次请求只需要带上登录信息 ID（Session_ID）到服务器上找有没有对应的登录信息（Session）即可。

3、如果找到了登录信息（Session）则表示通过验证。

对于单个服务的应用来说没有什么问题，如果要扩展为分布式应用。例如，一个公司有 A、B 两个服务，需要要实现用户在任意一个服务登录后就可以访问两个服务。实现思路是把 Session 持久化，比如写入数据库或者 Redis 缓存，验证的时候去数据库或者 Redis 缓存找 Session，定时清理数据库或者设置 Redis 过期时间。

**基于 Token 的认证方法**

这种方法，不在服务端保存用户的登录信息，用户 Token 令牌保存在客户端。客户端请求带上 Token 信息，服务端验证 Token 的有效性即可。

1、用户输入用户名、用户密码，验证之后加密生成 Token

2、客户端保存 Token，每次请求的时候带上此 Token

3、服务端根据密钥验证 Token 的有效性

对于  A、B 两个服务如果使用了相同的加密算法和密钥，那么任意一个服务生成的 Token 都可以直接在另一个服务使用，即使新增一个 C 服务，只要加密算法和密钥相同，三个服务的 Token 还是通用的。

对于想用户更改密码后使未过期的 Token 失效的需求，可以通过在用户表新增一个用户修改信息的时间戳，只要 Token 是在这个时间戳前生成的，直接默认无效；还有一种方法也是增加一个 Redis 缓存，更改用户信息后删除该用户的 Token 缓存，缓存中没有用户 Token 直接默认失效。

对于配置单点登录的方法，我目前的思路还是只有 Redis 缓存这一个(手动捂脸)

### 1.2）Token 长什么样的？

Token 由三部分构成

- Header（头部）
- Payload（负载）
- Signature（签名）

完整的 Token 示例，三部分由小数点  .  分割开：

```bash
eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJTcHJpbmdCb290IiwiYXVkIjoiYm9vdCIsImV4cCI6MTU4NjE5MzMwMywiaWF0IjoxNTg1NTg4NTAzLCJyb2xlIjoiQURNSU4ifQ.SdHSoet9BEaMcBrbbwO4_nd88nO7VIuV6IB_Kdw1AFmmPPCxY8CKUoE-QrJmN3RSMAdxLB0GAfDiwFV6zpxVZksXZQAQzxa_bPw0JUj7mZyHzdSR2jNm_oKB_2rnRsfW7caXZVgwtUU2lHoXSLdlgHRqoyIw7AcP5dm-og3ELGgUQxa27mmwvXtRngfvgw1EKoeA_bdwNSbDWu8clyNjd9ftq9_yU3QKFc3NAUVkWTRa8U1_dyOI9B4LMrKrXEQSR8D7UDw-0MDbOZNwzUmxv0h-QER1cw5dxnQsMs2C9TI32x9E68PaNC8PkaAyOkCs55y-W7wyf-K24fzt5nQb4w
```

> 需要注意的是，**头部和负载是不加密的，由 Bas64 算法将 JSON 格式的数据编码而来，因此不能在 Token 存储密码以及用户隐私信息**。

**Header 头部**

```json
{
  "alg": "HS256"
}
```

头部一般是声明 Token 的 Signature（签名） 的加密算法，经过 Base64 编码后得到 Token 的第一部分，示例如下：

```
eyJhbGciOiJIUzI1NiJ9
```

**Payload（负载）**

```json
{
  "sub": "Joe"
}
```

这是 Token 的主体部分，这里同样是经过 Base64 编码后得到 Token 的第二部分，示例如下：

```
eyJzdWIiOiJKb2UifQ.1KP0SsvENi7Uz1oQc07aXTL7kpQG5jBNIybqr60AlD4
```

需要注意的是 Payload（负载）不是加密的，请勿在这里存储任何私密信息，比如密码、用户隐私信息。

常用的设置如下，标准 Claims：

- `setIssuer`: Token 签发者
- `setSubject`: Token 主题
- `setAudience`: Token 接受者
- `setExpiration`: 过期时间
- `setNotBefore`: 在什么日期后才生效
- `setIssuedAt`:  签发时间
- `setId`:  唯一的标识符

自定义 Claims：

- `.claim("role", "ADMIN")` : 通过此方法自定义了一个名称为 role，值为 ADMIN 的 Claims

**Signature（签名）**

签名部分是对 Token 的 **Header 头部** 和 **Payload（负载）** 通过头部声明的加密方式和服务端定义密钥加密生成的，如果 **Header 头部** 和 **Payload（负载）**的数据发生改变，**Signature（签名）**随即发生改变，在密钥未泄露的情况，其他人**无法篡改或者伪造 Token**，这也是为什么 Token 是安全的。

```bash
SdHSoet9BEaMcBrbbwO4_nd88nO7VIuV6IB_Kdw1AFmmPPCxY8CKUoE-QrJmN3RSMAdxLB0GAfDiwFV6zpxVZksXZQAQzxa_bPw0JUj7mZyHzdSR2jNm_oKB_2rnRsfW7caXZVgwtUU2lHoXSLdlgHRqoyIw7AcP5dm-og3ELGgUQxa27mmwvXtRngfvgw1EKoeA_bdwNSbDWu8clyNjd9ftq9_yU3QKFc3NAUVkWTRa8U1_dyOI9B4LMrKrXEQSR8D7UDw-0MDbOZNwzUmxv0h-QER1cw5dxnQsMs2C9TI32x9E68PaNC8PkaAyOkCs55y-W7wyf-K24fzt5nQb4w
```

JJWT 是用于在 JVM 和 Android 上创建和验证 JSON Web Token(JWT) 的开源（Apache 2.0）工具包，是基于 JWT、JWS、 JWE、JWK 规范的 Java 实现。支持的加密算法有 HMAC，RSASSA，ECDSA，在开发过程中，必须使用对所选算法而言足够强的密钥。这一小节中，将采用 RSA 非对称加密的方式加密 Token。

## 2）JJWT 依赖引入和项目配置

新建项目 06-security-token，**注意 Spring Boot 的版本要为 2.1.X 版本**，记得勾选 Spring Security，MySQL，MyBatis，Web 依赖。

对于 Maven 项目，同样是在文章最后面给出对应的依赖配置。

**Gradle 项目配置**

```json
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.1.2'
    runtimeOnly 'mysql:mysql-connector-java'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    // 添加如下的依赖 https://github.com/jwtk/jjwt
    compile 'io.jsonwebtoken:jjwt-api:0.11.1'
    runtime 'io.jsonwebtoken:jjwt-impl:0.11.1',
            // Uncomment the next line if you want to use RSASSA-PSS (PS256, PS384, PS512) algorithms:
            //'org.bouncycastle:bcprov-jdk15on:1.60',
            // or 'io.jsonwebtoken:jjwt-gson:0.11.1' for gson
            'io.jsonwebtoken:jjwt-jackson:0.11.1'
```

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

## 2）开始使用 JJWT

项目的主要结构：

- controller 包：API 接口
- service 包：为 API 提供接口服务
- mapper 包：MyBatis Mapper 类
- entity 包：实体类
- security 包：Token 拦截验证、Token 生成、Spring Security 配置

MyResponse ：公共 Response 返回消息类：

```java
public class MyResponse implements Serializable {
    private static final long serialVersionUID = -2L;
    private String status;
    private String message;
}
```

### 2.1）实体类 Entity 和 Mapper

这里的表结构和上一节一样， 用户表 sys_user


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

 **Entity 实体类**：新建 package，名称为 entity 。在 entity下新建一个 SysUser 类：

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

**Mapper 接口类**：新建包 mapper，新建 SysUserMapper 类：

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

## 2.2）Token 配置

如以下几个部分构成

- RSA 密钥公钥工具类
- Token 生成、验证工具类
- Token 拦截器，拦截所有请求并验证 Token 是否有效
- 自定义未实现授权访问错误处理类
- 自定义实现 UserDetailsService 接口
- Spring Security 配置

在 Security 包下新建 RsaUtils 类，RSA 的公钥和密钥的工具类。注意在 JDK 8 中，2048 位的密钥不受支持。

```java
public class RsaUtils {
    /** PrivateKey * 生成秘钥 > openssl genrsa -out rsa_private_key.pem 2048 
     * 转换成PKCS8格式 >openssl pkcs8 -topk8 -inform * PEM -in rsa_private_key.pem -outform PEM -nocrypt 
     * 在终端输出结果，去掉“-----BEGIN PRIVATE KEY-----” * “-----END PRIVATE KEY-----”
     * @return PrivateKey
     */
    public static PrivateKey getPrivateKey() {
        PrivateKey privateKey = null;
        try {
            String privateKeyStr = "PrivateKey";
            // PKCS8格式的密钥
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr));
            // RSA 算法
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    /** PublicKey 根据 秘钥 生成public key > openssl rsa -in rsa_private_key.pem -out rsa_public_key.pem -pubout
     * @return PublicKey
     */
    public static PublicKey getPublicKey() {
        PublicKey publicKey = null;
        try {
            String publicKeyStr = "public key";
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyStr));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }
}
```

**TokenUtils** : 生成和验证 Token 的工具类。可选的 Token 主体部分是指在验证 和授权的时候用不上这些信息：

```java
@Component
public class TokenUtils implements Serializable {
    private static final long serialVersionUID = -3L;
    /**
     * Token 有效时长
     */
    private static final Long EXPIRATION = 604800L;

    /** 生成 Token 字符串 必须 setAudience 接收者 setExpiration 过期时间 role 用户角色
     * @param sysUser 用户信息
     * @return 生成的Token字符串 or null
     */
    public String createToken(SysUser sysUser) {
        try {
            // Token 的过期时间
            Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION * 1000);
            // 生成 Token
            String token = Jwts.builder()
                    // 设置 Token 签发者 可选
                    .setIssuer("SpringBoot")
                    // 根据用户名设置 Token 的接受者
                    .setAudience(sysUser.getUsername())
                    // 设置过期时间
                    .setExpiration(expirationDate)
                    // 设置 Token 生成时间 可选
                    .setIssuedAt(new Date())
                    // 通过 claim 方法设置一个 key = role，value = userRole 的值
                    .claim("role", sysUser.getUserRole())
                    // 设置加密密钥和加密算法，注意要用私钥加密且保证私钥不泄露
                    .signWith(RsaUtils.getPrivateKey(), SignatureAlgorithm.RS256)
                    .compact();
            return String.format("Bearer %s", token);
        } catch (Exception e) {
            return null;
        }
    }

    /** 验证 Token ，并获取到用户名和用户权限信息
     * @param token Token 字符串
     * @return sysUser 用户信息
     */
    public SysUser validationToken(String token) {
        try {
            // 解密 Token，获取 Claims 主体
            Claims claims = Jwts.parserBuilder()
                    // 设置公钥解密，以为私钥是保密的，因此 Token 只能是自己生成的，如此来验证 Token
                    .setSigningKey(RsaUtils.getPublicKey())
                    .build().parseClaimsJws(token).getBody();
            assert claims != null;
            // 验证 Token 有没有过期 过期时间
            Date expiration = claims.getExpiration();
            // 判断是否过期 过期时间要在当前日期之后
            if (!expiration.after(new Date())) {
                return null;
            }
            SysUser sysUser = new SysUser();
            sysUser.setUsername(claims.getAudience());
            sysUser.setUserRole(claims.get("role").toString());
            return sysUser;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
```

**TokenFilter** ：Token 拦截器，拦截所有请求并验证 Token 是否有效，有效则授权通过，无效则由 Spring Security 根据配置拦截无效请求:

```java
@SuppressWarnings("SpringJavaAutowiringInspection")
@Service
public class TokenFilter extends OncePerRequestFilter {
    @Resource
    TokenUtils tokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 存储 Token 的 Headers Key与 Value，默认是 Authorization
        final String authorizationKey = "Authorization";
        String authorizationValue;
        try {
            authorizationValue = request.getHeader(authorizationKey);
        } catch (Exception e) {
            authorizationValue = null;
        }
        // Token 开头部分 默认 Bearer 开头
        String bearer = "Bearer ";
        if (authorizationValue != null && authorizationValue.startsWith(bearer)) {
            // token
            String token = authorizationValue.substring(bearer.length());
            SysUser sysUser = tokenUtils.validationToken(token);
            if (sysUser != null) {
                // Spring Security 角色名称默认使用 "ROLE_" 开头
                // authorities.add 可以增加多个用户角色，对于一个用户有多种角色的系统来说，
                // 可以通过增加用户角色表、用户--角色映射表，存储多个用户角色信息
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + sysUser.getUserRole()));
                // 传入用户名、用户密码、用户角色。 这里的密码随便写的，用不上
                UserDetails userDetails = new User(sysUser.getUsername(), "password", authorities);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(userDetails.getUsername());
                // 授权
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

**ErrorAuthenticationEntryPoint**：错误消息类，未授权访问通过此类返回 401 未授权访问信息。自定义实现 Spring Security 的默认未授权访问处理接口 AuthenticationEntryPoint:

```java
@Component
public class ErrorAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {
    private static final long serialVersionUID = 5200068540912465653L;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 设置 Json 格式返回
        response.setContentType("application/json;charset=UTF-8");
        // 设置 HTTP 状态码为 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // PrintWriter 输出 Response 返回信息
        PrintWriter writer = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        MyResponse myResponse = new MyResponse("error", "非授权访问");
        // 将对象输出为 JSON 格式。可以通过重写 MyResponse 的 toString() ，直接通过 myResponse.toString() 即可
        writer.write(mapper.writeValueAsString(myResponse));
    }
}
```

**UserDetailsServiceImpl** ：自定义实现 UserDetailsService，通过重写UserDetailsService接口的loadUserByUsername 方法，给 Spring Security 传入用户名、用户密码、用户角色。

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (sysUser == null ) {
            throw new UsernameNotFoundException(username);
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // Spring Security 角色名称默认使用 "ROLE_" 开头
        // authorities.add 可以增加多个用户角色，对于一个用户有多种角色的系统来说，
        // 可以通过增加用户角色表、用户--角色映射表，存储多个用户角色信息
        authorities.add(new SimpleGrantedAuthority("ROLE_" + sysUser.getUserRole()));
        // 给 Spring Security 传入用户名、用户密码、用户角色。
        return new User(sysUser.getUsername(), sysUser.getPassword(), authorities);
    }
}
```

**SpringSecurityConfig** ：Spring Security 配置，配置密码存储加密算法，添加拦截器，关闭 Session 管理器，允许跨域访问，允许登录和注册的 API 无授权访问。

```java
@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Resource
    private ErrorAuthenticationEntryPoint errorAuthenticationEntryPoint;

    @Resource
    private TokenFilter tokenFilter;

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        // 使用 BCryptPasswordEncoder 验证密码
        authenticationManagerBuilder.userDetailsService(this.userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 密码
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        // 配置 CSRF 关闭,允许跨域访问
        httpSecurity.csrf().disable();
        // 指定错误未授权访问的处理类
        httpSecurity.exceptionHandling().authenticationEntryPoint(errorAuthenticationEntryPoint);
        // 关闭 Session
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 允许 登录 注册的 api 的无授权访问，其他需要授权访问
        httpSecurity.authorizeRequests()
                .antMatchers("/api/user/login", "/api/user/register")
                .permitAll().anyRequest().authenticated();
        // 添加拦截器
        httpSecurity.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        // 禁用缓存
        httpSecurity.headers().cacheControl();
    }
}
```

至此，Token 的安全配置结束。

### 2.3）API 接口和接口服务层

新建 service 包，新建接口服务层 SysUserService：

```java
@Service
public class SysUserService {
    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private TokenUtils tokenUtils;

    @Resource
    private SysUserMapper sysUserMapper;

    /** 用户登录
     * @param sysUser 用户登录信息
     * @return 用户登录成功返回的Token
     */
    public MyResponse login(final SysUser sysUser) {
        try {
            // 验证用户名和密码是否对的
            System.out.println(sysUser.getUsername());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(sysUser.getUsername(),
                            sysUser.getPassword()));
        } catch (BadCredentialsException e) {
            return new MyResponse("ERROR", "用户名或者密码不正确");
        }
        // 生成Token与查询用户权限
        SysUser sysUserData = sysUserMapper.selectByUsername(sysUser.getUsername());
        return new MyResponse("SUCCESS",
                tokenUtils.createToken(sysUserData));
    }

    /** 用户注册
     * @param sysUser 用户注册信息
     * @return 用户注册结果
     */
    public MyResponse save(SysUser sysUser) throws DataAccessException {
        try {
            // 密码加密存储
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String password = bCryptPasswordEncoder.encode(sysUser.getPassword());
            sysUser.setPassword(password);
            sysUserMapper.insert(sysUser);
        } catch (DataAccessException e) {
            return new MyResponse("ERROR", "已经存在该用户名或者用户昵称，或者用户权限出错");
        }
        return new MyResponse("SUCCESS", "用户新增成功");
    }
}
```

**SysUserController** ：RESTful API 接口， @PreAuthorize("hasRole('ADMIN')") 指定具有 ADMIN 权限的用户才可以访问

```java
@RestController
@RequestMapping(value = "/api/user")
public class SysUserController {
    @Resource
    private SysUserService sysUserService;

    /** 用户登录接口
     * @param sysUser 用户登录的用户名和密码
     * @return 用户Token和角色
     * @throws AuthenticationException 身份验证错误抛出异常
     */
    @PostMapping(value = "/login")
    public MyResponse login(@RequestBody final SysUser sysUser) throws AuthenticationException {
        return sysUserService.login(sysUser);
    }

    /** 用户注册接口
     * @param sysUser 用户注册信息
     * @return 用户注册结果
     */
    @PostMapping(value = "/register")
    public MyResponse register(@RequestBody @Valid final SysUser sysUser) {
        return sysUserService.save(sysUser);
    }

    /** 这是登录用户才可以看到的内容 */
    @PostMapping(value = "/message")
    public String message() {
        return "这个消息只有登录用户才可以看到";
    }

    /** 这是管理员用户才可以看到 */
    @PostMapping(value = "/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "这个消息只有管理员用户才可以看到";
    }
}
```

### 2.4）运行

运行项目分别注册、登录和访问授权用户接口。

**注册：** 分别注册 ADMIN、USER 权限的用户

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200402005832.png)

**登录：**

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200402005937.png)

使用 Token 授权：/api/user/admin  和 /api/user/message

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200402003800.png)



附录，，Maven 项目配置

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
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
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.1</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.1</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>        <!-- or jjwt-gson if Gson is preferred -->
        <version>0.11.1</version>
        <scope>runtime</scope>
    </dependency>
    <!-- Uncomment this next dependency if you are using JDK 10 or earlier and you also want to use 
RSASSA-PSS (PS256, PS384, PS512) algorithms.  JDK 11 or later does not require it for those algorithms:
<dependency>
<groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.60</version>
<scope>runtime</scope>
</dependency> -->
</dependencies>
```



本小节主要实战了 Spring Boot 整合 Spring Security，JJWT，实现 Token 认证和授权的 RESTful API 接口，实现用户的注册、登录和角色控制的功能。下一小节，将实战 Spring Boot 整合另一个常用的安全框架 **Apache Shiro** 实现 Token 认证和授权的 RESTful API 接口。接下来的序列文章安排

- Apache Shiro 实现 Token 认证和授权的 RESTful API
- Apache Shiro 实现微信扫码登录



