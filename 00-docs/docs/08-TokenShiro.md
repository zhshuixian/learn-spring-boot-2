# Spring Boot 2.X 实战--Shiro(Token)登录和注册

> 博客主页：https://me.csdn.net/u010974701
>
> 源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)

在上一节"Spring Security (Token)登录和注册"中，主要介绍了 Spring Boot 整合 Spring Security 实现 Token 的登录和认证，这一小节中，我们将实现 Spring Boot 整合 Shiro 实现 Token 的登录和认证。

## 1）Apache Shiro 简介

在前面介绍过，Java 开发常用的安全框架有 Spring Security 和 Apache Shiro，这里将简要介绍一下 Shiro，Shiro 是一个功能强大的开源安全框架：

> Apache Shiro&trade;**是一个功能强大且易于使用的 Java 安全框架，用于执行身份验证，授权，加密和会话管理。使用Shiro易于理解的API，您可以快速轻松地保护任何应用程序-从最小的移动应用程序到最大的Web和企业应用程序。

对于使用 Shiro，需要了解其三个核心概念：

- Subject：主题，是一个安全术语，表示"当前正在执行的用户"
- SecurityManager :  安全管理器，是 Shiro 的核心，提供各种安全管理的服务和管理所有的 Subject。
- Realm : Realm 是应用程序和安全数据之间的"桥梁"或者"连接器"，当 Shiro 需要和安全数据（例如：用户账户信息）交互以实现身份验证（登录认证）和授权（访问控制），Shiro 会通过其配置的一个或者多个 Realm 实现。

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200410002311.png)

> 扩展阅读：http://shiro.apache.org/architecture.html

## 2）Shiro 项目配置

新建一个项目，*07-shiro*，记得勾选 MySQL，MyBatis，Web 依赖。对于 Maven 项目，同样是在文章最后面给出对应的依赖配置。

**Gradle 项目配置**

```json
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.1.2'
    runtimeOnly 'mysql:mysql-connector-java'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    // https://mvnrepository.com/artifact/org.apache.shiro/shiro-spring-boot-web-starter
    compile group: 'org.apache.shiro', name: 'shiro-spring-boot-web-starter', version: '1.5.2'
    // https://github.com/jwtk/jjwt
    compile 'io.jsonwebtoken:jjwt-api:0.11.1'
    runtime 'io.jsonwebtoken:jjwt-impl:0.11.1',
            // Uncomment the next line if you want to use RSASSA-PSS (PS256, PS384, PS512) algorithms:
            //'org.bouncycastle:bcprov-jdk15on:1.60',
            // or 'io.jsonwebtoken:jjwt-gson:0.11.1' for gson
            'io.jsonwebtoken:jjwt-jackson:0.11.1'
```

### 2.1）项目配置

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

## 3）开始使用 Shiro

项目的主要结构：

- controller 包：API 接口
- service 包：为 API 提供接口服务
- mapper 包：MyBatis Mapper 类
- entity 包：实体类
- Shiro 包：Token 拦截验证、Token 生成、Shiro 的 Realm 等配置

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


| 字段            | 类型         | 备注                   |
| --------------- | ------------ | ---------------------- |
| user_id         | bigint       | 自增主键               |
| username        | varchar(18)  | 用户名，非空唯一       |
| password        | varchar(128) | 密码，非空             |
| user_role       | varchar(8)   | 用户角色(USER / ADMIN) |
| user_permission | varchar(36)  | 用户权限               |

> 这里用户角色有 USER / ADMIN ，对于一个用户可能有多个角色的情况暂不考虑。
>
> Shiro 可以指定相应的权限控制，比如 Update 的权限，Create 的权限，使得可以更加细粒度的控制。这里的权限用英文分号 , 直接隔开，例如：update,create,delete ，表示该用户具有 Update 等三个权限。
>
> 密码使用 HASH 散列加密。

SQL

```
create table sys_user
(
    user_id         bigint auto_increment,
    username        varchar(18)  not null unique,
    password        varchar(128) not null,
    user_role       varchar(8)   not null,
    user_permission varchar(36)   not null,
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
    private String userPermission;
    // 省略 getter setter constructor
}
```

**Mapper 接口类**：新建包 mapper，新建 SysUserMapper 类：

```java
public interface SysUserMapper {
    /** 往 sys_user 插入一条记录
     * @param sysUser 用户信息
     */
    @Insert("Insert Into sys_user(username, password,user_role,user_permission) Values(#{username}, #{password},#{userRole},#{userPermission})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insert(SysUser sysUser);
    /** 根据用户 Username 查询用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @Select("Select user_id,username, password,user_role,user_permission From sys_user Where username=#{username}")
    SysUser selectByUsername(String username);
}
```

### 2.2）Token 配置

首先实现 Token 生成和验证的功能：

- RSA 密钥公钥工具类
- Token 生成、验证工具类

在 shiro 包下新建 **RsaUtils** 类，RSA 的公钥和密钥的工具类。注意在 JDK 8 中，2048 位的密钥不受支持。代码参考 **Spring Boot 2.X 实战--Spring Security (Token)登录和注册** 的 **2.2）Token 配置** 的小节：

**TokenUtils** : 生成和验证 Token 的工具类。可选的 Token 主体部分是指在验证和授权的时候用不上这些信息，主要的代码和上一节差不多，主要是增加一个 Refresh 刷新 Token 的功能：

```java
@Component
public class TokenUtils implements Serializable {
    private static final long serialVersionUID = -3L;
    /** Token 有效时长 多少秒 **/
    private static final Long EXPIRATION = 2 * 60L;

    /** 生成 Token 字符串  setAudience 接收者 setExpiration 过期时间 role 用户角色
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
                    // 用户角色
                    // 通过 claim 方法设置一个 key = permission，value = Permission 的值
                    .claim("permission", sysUser.getUserPermission())
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
            SysUser sysUser = new SysUser();
             // 获得用户信息
            sysUser.setUsername(claims.getAudience());
            sysUser.setUserRole(claims.get("role").toString());
            sysUser.setUserPermission(claims.get("permission").toString());
            return sysUser;
        } catch (Exception e) {
            return null;
        }
    }
}
```

Token 刷新部分在后面单独来讲。

### 2.3）Shiro 配置

实现 Shiro 的 Realm，拦截器等

- ShiroAuthToken ：实现 AuthenticationToken 接口
- ShiroRealm ：自定义 Realm，验证 Token 和从 Token 中取得用户角色和权限
- ShiroAuthFilter ：拦截器，拦截所有请求，并验证 Token
- ShiroConfig ：Shiro 配置，将 Realm、拦截器等配置到 SecurityManager 中

**ShiroAuthToken** ：实现 AuthenticationToken 接口，作为 Token 传入到 Realm 的载体：

```java
public class ShiroAuthToken implements AuthenticationToken {
    private String token;
    public ShiroAuthToken(String token) { this.token = token; }
    
    @Override
    public Object getPrincipal() { return token;  }

    @Override
    public Object getCredentials() { return token; }
}
```

**ShiroRealm** ：从 ShiroAuthToken 取得 Token 并进行身份验证和角色权限配置。

```java
@Service
public class ShiroRealm extends AuthorizingRealm {
    @Resource
    TokenUtils tokenUtils;

    @Override
    public boolean supports(AuthenticationToken authenticationToken) {
        // 指定当前 authenticationToken 需要为 ShiroAuthToken 的实例
        return authenticationToken instanceof ShiroAuthToken;
    }
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        ShiroAuthToken shiroAuthToken = (ShiroAuthToken) authenticationToken;
        String token = (String) shiroAuthToken.getCredentials();
        // 验证 Token
        SysUser sysUser = tokenUtils.validationToken(token);
        if (sysUser == null || sysUser.getUsername() == null || sysUser.getUserRole() == null) {
            throw new AuthenticationException("Token 无效");
        }
        return new SimpleAuthenticationInfo(token,
                token, "ShiroRealm");
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 获取用户信息
        SysUser sysUser = tokenUtils.validationToken(principals.toString());
        // 创建一个授权对象
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 判断用户角色是否存在
        if (!sysUser.getUserRole().isEmpty()) {
            // 角色设置
            info.addRole(sysUser.getUserRole());
        }
        if (!sysUser.getUserPermission().isEmpty()) {
            // 进行权限设置,根据 , 分割
            Arrays.stream(sysUser.getUserPermission().split(",")).forEach(info::addStringPermission);
        }
        return info;
    }
}
```

**代码解析：**

ShiroRealm 继承了 AuthorizingRealm，必须覆写 doGetAuthenticationInfo 和 doGetAuthorizationInfo 两个方法。通过覆写 supports 方法，指定 authenticationToken 必须是我们刚才定义的 ShiroAuthToken 的实例。

doGetAuthenticationInfo 的方法主要是从 authenticationToken  取得 Token，并进行 Token 验证和用户授权。

doGetAuthorizationInfo 的方法主要是实现用户角色、用户权限的配置，对于没有用户角色、权限的系统来说，可以不实现，直接 super。

实现 Token 的拦截器。

**ShiroAuthFilter** ：Shiro 的拦截器，拦截和验证 Token 的有效性

```java
public class ShiroAuthFilter extends BasicHttpAuthenticationFilter {

    /**
     * // 存储Token的H Headers Key
     */
    protected static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Token 的开头部分
     */
    protected static final String BEARER = "Bearer ";

    private String token;


    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        // 设置 主题
        // 自动调用 ShiroRealm 进行 Token 检查
        this.getSubject(request, response).login(new ShiroAuthToken(this.token));
        return true;
    }

    /**  是否允许访问
     * @param request     Request
     * @param response    Response
     * @param mappedValue mapperValue
     * @return true 表示允许放翁
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // Request 中存在 Token
        if (this.getAuthzHeader(request) != null) {
            try {
                executeLogin(request, response);
                // 刷新 Token 1, Token 未过期，每次都调用 refreshToken 判断是否需要刷新 Token
                TokenUtils tokenUtils = new TokenUtils();
                String refreshToken = tokenUtils.refreshToken(this.token);
                if (refreshToken != null) {
                    this.token = refreshToken;
                    shiroAuthResponse(response, true);
                }
                return true;
            } catch (Exception e) {
                // 刷新 Token 2, Token 已经过期，如果过期是在规定时间内则刷新 Token
                TokenUtils tokenUtils = new TokenUtils();
                String refreshToken = tokenUtils.refreshToken(this.token);
                if (refreshToken != null) {
                    this.token = refreshToken.substring(BEARER.length());
                    // 重新调用 executeLogin 授权
                    executeLogin(request, response);
                    shiroAuthResponse(response, true);
                    return true;
                } else {
                    // Token 刷新失败没得救或者非法 Token
                    shiroAuthResponse(response, false);
                    return false;
                }
            }
        } else {
            // Token 不存在，返回未授权信息
            shiroAuthResponse(response, false);
            return false;
        }
    }

    /** Token 预处理，从 Request 的 Header 取得 Token
     * @param request ServletRequest
     * @return token or null
     */
    @Override
    protected String getAuthzHeader(ServletRequest request) {
        try {
            // header 是否存在 Token
            HttpServletRequest httpRequest = WebUtils.toHttp(request);
            this.token = httpRequest.getHeader(AUTHORIZATION_HEADER).substring(BEARER.length());
            return this.token;
        } catch (Exception e) {
            return null;
        }
    }

    /** 未授权访问或者 Header 添加 Token
     * @param response Response
     * @param refresh  是否是刷新 Token
     */
    private void shiroAuthResponse(ServletResponse response, boolean refresh) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (refresh) {
            // 刷新 Token，设置返回的头部
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setHeader("Access-Control-Expose-Headers", "Authorization");
            httpServletResponse.addHeader(AUTHORIZATION_HEADER, BEARER + this.token);
        } else {
            // 设置 HTTP 状态码为 401
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            // 设置 Json 格式返回
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            try {
                // PrintWriter 输出 Response 返回信息
                PrintWriter writer = httpServletResponse.getWriter();
                ObjectMapper mapper = new ObjectMapper();
                MyResponse myResponse = new MyResponse("error", "非授权访问");
                // 将对象输出为 JSON 格式。可以通过重写 MyResponse 的 toString() ，直接通过 myResponse.toString() 即可
                writer.write(mapper.writeValueAsString(myResponse));
            } catch (IOException e) {
                // 打印日志
            }
        }
    }
}
```

**Token 刷新策略**：目前小先想到的 Token 刷新策略有以下几种

- 后端提供一个刷新 Token 的接口，前端根据浏览器缓存的 token 过期时间，如 Token 不到 1 天就要过期就访问刷新 Token 的接口。前端实现无，后端实现参考用户登录部分接口和 Token 刷新代码部分
- 后端判断 Token 快要过期了就刷新 Token ，并放入到 Response 的 Header，详情看代码 ```// 刷新 Token 1```，坏处是每次都要判断是否要刷新 token
- 后端判断 Token 在 Token 过期后，如果在指定的时间范围内，则可以刷新 Token，并把新 Token 放入到 Response 的 Header，详情看代码 ```// 刷新 Token 2``` ，坏处是要自己手动判断 Token 是否合法
- 其他，还没有想到，欢迎您的留言

回到 **TokenUtils**  这个类，新增  refreshToken 的方法，特别要注意代码注释中的 ```// TODO  需要自己用 RSA 算法验证 Token 的合法性``` 这一部分，如果没有用加密算法验证 Token 是不是自己签发的，伪造的 Token 可以通过方法三骗取合法 Token，感兴趣的读者可以自行试试。

```java
/** Token 刷新
 * @param token 就 Token
 * @return String 新 Token 或者 null
 */
public String refreshToken(String token) {
    try {
        // 解密 Token，获取 Claims 主体
        Claims claims = Jwts.parserBuilder()
                // 设置公钥解密，以为私钥是保密的，因此 Token 只能是自己生成的，如此来验证 Token
                .setSigningKey(RsaUtils.getPublicKey())
                .build().parseClaimsJws(token).getBody();
        // 刷新 Token 1 下面代码是未到期刷新
        // 可以更改代码，在验证的 Token 的时候直接判断是否要刷新 Token
        assert claims != null;
        // Token 过期时间
        Date expiration = claims.getExpiration();
        // 如果 1 分钟内过期，则刷新 Token
        if (!expiration.before(new Date(System.currentTimeMillis() + 60 * 1000))) {
            // 不用刷新
            return null;
        }
        SysUser sysUser = new SysUser();
        sysUser.setUsername(claims.getAudience());
        sysUser.setUserRole(claims.get("role").toString());
        sysUser.setUserPermission(claims.get("permission").toString());
        // 生成新的 Token
        return createToken(sysUser);
    } catch (ExpiredJwtException e) {
        // 刷新 Token 2 ：Token 在解密的时候会自动判断是否过期
        // 过期 ExpiredJwtException 可以通过 e.getClaims() 取得 claims
        // 实际中千万不要直接这么用
        // TODO  需要自己用 RSA 算法验证 Token 的合法性
        try {
            Claims claims = e.getClaims();
            // 如果 claims 不为空表示 Token 正常解析出了主题部分
            assert claims != null;
            // Token 过期时间
            Date expiration = claims.getExpiration();
            // 如果过期时间在 10 分钟内，则刷新 Token
            if (!expiration.after(new Date(System.currentTimeMillis() - 10 * 60 * 1000))) {
                // 超过 10 分钟，没得救了
                return null;
            } else {
                SysUser sysUser = new SysUser();
                sysUser.setUsername(claims.getAudience());
                sysUser.setUserRole(claims.get("role").toString());
                sysUser.setUserPermission(claims.get("permission").toString());
                return createToken(sysUser);
            }
        } catch (Exception e1) {
            return null;
        }
    }
}
```

**ShiroConfig** ：配置 Shiro 的 Realm 和拦截器、拦截规则，关闭 Session 。对于 Shiro 而言，必须配置名称为 securityManager 和 shiroFilterFactoryBean 的 Bean，Shiro 的启动器 Starter 的应该怎么配置在研究中。不加会报如下错误：

```bash
required a bean named 'shiroFilterFactoryBean' that could not be found.
```

ShiroConfig 代码：

```java
@Configuration
public class ShiroConfig {
    /** 使用自定义的 Realm 和关闭 Session 管理器
     * @param realm 自定义的 Realm
     * @return SecurityManager
     */
    @Bean
    public DefaultWebSecurityManager securityManager(ShiroRealm realm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        // 使用自己的 realm
        manager.setRealm(realm);
        // 关闭 Session
        // shiro.ini 方式参考 http://shiro.apache.org/session-management.html#disabling-subject-state-session-storage
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        manager.setSubjectDAO(subjectDAO);
        return manager;
    }

    /** 添加拦截器和配置拦截规则
     * @param securityManager 安全管理器
     * @return 拦截器和拦截规则
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);
        Map<String, Filter> filters = new HashMap<>(2);
        //  添加 shiroAuthFilter 的拦截器，不要使用 Spring 来管理 Bean
        filters.put("authFilter", new ShiroAuthFilter());
        factoryBean.setFilters(filters);
        // 一定要用 LinkedHashMap，HashMap 顺序不一定按照 put 的顺序，拦截匹配规则是从上往下的
        // 比如 /api/user/login ，已经匹配到了，即使用 anon 的拦截器，就不会再去匹配 /** 了
        // anon 支持匿名访问的拦截器
        LinkedHashMap<String, String> filterChainDefinitions = new LinkedHashMap<>(4);
        // 登录接口和注册放开
        filterChainDefinitions.put("/api/user/login", "anon");
        filterChainDefinitions.put("/api/user/register", "anon");
        // 其他请求通过自定义的 authFilter
        filterChainDefinitions.put("/**", "authFilter");
        factoryBean.setFilterChainDefinitionMap(filterChainDefinitions);
        return factoryBean;
    }
}
```

### 2.4）登录和注册接口

SysUserService：API 接口服务层

```java
@Service
public class SysUserService {
    /** Hash 加密的盐 **/
    private final String SALT = "#4d1*dlmmddewd@34%";
    @Resource private TokenUtils tokenUtils;
    @Resource private SysUserMapper sysUserMapper;

    /** 用户登录 **/
    public MyResponse login(SysUser sysUser) {
        // 从 数据库查询用户信息
        SysUser user = sysUserMapper.selectByUsername(sysUser.getUsername());
        if (user == null || user.getUsername() == null || user.getPassword() == null
                || user.getUserRole() == null || user.getUserPermission() == null) {
            return new MyResponse("error", "用户信息不存在");
        }
        String password = new SimpleHash("SHA-512", sysUser.getPassword(), this.SALT).toString();
        if (!password.equals(user.getPassword())) {
            return new MyResponse("error", "密码错误");
        }
        // 生成 Token
        return new MyResponse("SUCCESS",
                tokenUtils.createToken(user));
    }

    /** 用户注册
     * @param sysUser 用户注册信息
     * @return 用户注册结果
     */
    public MyResponse save(SysUser sysUser) throws DataAccessException {
        try {
            // 密码加密存储
            String password = new SimpleHash("SHA-512", sysUser.getPassword(), this.SALT).toString();
            sysUserMapper.insert(sysUser);
        } catch (DataAccessException e) {
            return new MyResponse("ERROR", "已经存在该用户名或者用户昵称，或者用户权限出错");
        }
        return new MyResponse("SUCCESS", "用户新增成功");
    }
}
```

这里登录逻辑没有使用 Shiro 的 Realm 来实现，密码存储采用 SHA-512 算法加密用户名存储。对于 Shiro 密码服务功能还在探索中。

**SysUserController**：API 登录和注册接口

```java
@RestController
@RequestMapping("/api/user")
public class SysUserController {
    /** 存储Token的H Headers Key **/
    protected static final String AUTHORIZATION_HEADER = "Authorization";
    @Resource SysUserService sysUserService;

    /** 用户登录接口
     * @param sysUser 用户登录的用户名和密码
     * @return 用户Token和角色
     */
    @PostMapping(value = "/login")
    public MyResponse login(@RequestBody final SysUser sysUser, ServletResponse response) {
        MyResponse myResponse = sysUserService.login(sysUser);
        // 如果登录成功
        // 将 Token 写入到 Response 的 Header，方便前端刷新 Token 从 Header 取值
        if ("SUCCESS".equals(myResponse.getStatus())) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.addHeader(AUTHORIZATION_HEADER, myResponse.getMessage());
        }
        return myResponse;
    }

    @PostMapping("/register")
    public MyResponse register(@RequestBody SysUser sysUser) {
        return sysUserService.save(sysUser);
    }
    
    @GetMapping("/hello")
    public String hello() {
        return "已经登录的用户可见";
    }
}
```

运行项目，访问注册和登录的 API，注册 JSON 参考：

```json
{
    "username": "user",
    "password": "spring",
    "userRole": "USER",
    "userPermission":"writer,read"
}
```

为了方便测试，Token 的有效期设置为 2 分钟，对于过期时间在 1 分钟内或者过期 10 分钟的 Token，访问 /api/user/hello 接口会在 Response 的 Header 中返回新的 Token。

![](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200410103804.png)

### 2.5）Shiro 的权限和角色

**SysUserController**：API 登录和注册接口新加如下的接口：

```java
@RequiresRoles("ADMIN")
@PostMapping("/admin")
public String admin() {
    return "Admin 的用户角色可以见";
}

@RequiresPermissions("update")
@GetMapping("/permission")
public String permission() {
    return "需要 update 的权限才能访问";
}
```

Shiro 角色和权限的设置在 ShiroRealm 的 doGetAuthorizationInfo 的方法中。

重新运行项目，分别用不同的角色和权限的用户访问 admin 和 permission 接口。

**小结**

这一章中，主要实现了 Spring Boot 整合Shiro 实现 Token 的登录和验证，以及角色和权限的访问控制。下面的文章安排如下：

- 微信扫码登录
- Spring Boot 的异常拦截：统一拦截、封装异常信息返回给前端。

**附录：Maven 项目配置**

```xml
1<!-- https://mvnrepository.com/artifact/org.apache.shiro/shiro-spring-boot-web-starter -->
2<!-- 添加如下依赖 -->
3<dependency>
4    <groupId>org.apache.shiro</groupId>
5    <artifactId>shiro-spring-boot-web-starter</artifactId>
6    <version>1.5.2</version>
7</dependency>
```