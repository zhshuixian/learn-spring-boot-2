# Spring Boot  2.x 实战--第一个Spring Boot程序

《Spring Boot 2.X 实战》系列文章将分为如下几个模块，本小节将实战如何构建 RESTful API，并自定义返回数据和HTTP 返回码、以及给 API 接口传入数据，下一小节将实战 Spring Boot 整合 Log4j2 与 Slf4j 实现日志打印和输出到文件：

![Spring Boot 2.X 实战](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200205220414.png)

我是小先，一个专注大数据、分布式技术的非斜杠青年，爱Coding，爱阅读、爱摄影，更爱生活！

源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)

CSDN主页：[https://me.csdn.net/u010974701](https://me.csdn.net/u010974701)

> 上一章中主要介绍了 Spring Boot 和如何在 IDEA 中创建 Spring Boot 项目，本章将在上一章的基础上，介绍如何运行 Spring Boot 项目，并编写一些 RESTful API，本章主要包含如下内容：
> - 运行 Spring Boot 项目
> - 编写 RESTful API 接口
> - 编写、运行单元测试
> - 设置端口号和 HTTPS
> - 打包成 Jar

## 1、运行 Spring Boot 程序

IDEA 在完成 Spring Boot 项目的依赖资源下载后，会自动配置 Spring Boot 的启动方式。可以通过快捷键 "Shift + F10" ，或者直接点击右上角的运行按钮。如果是社区版的 Idea，可以通过直接运行 ```@SpringBootApplication``` 注解的类，这个类会在项目创建的时候自动生成。

![运行 Spring Boot](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200130215923.png)

启动完成后，会在下方窗口出现如下提示：

```shell
Started BootApplication in 0.884 seconds (JVM running for 1.393)
```

打开 Postman，访问 [http://localhost:8080](http://localhost:8080)会出现如下提示，这不是因为程序出错，我们还没有编写任何 API 接口。

![Postman 访问 Spring Boot](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200130221630.png)

### 1.1、Spring Boot 入口类和 @SpringBootApplication

Spring Boot 项目在新建的时候会自动生成入口类，默认名称为 \*Application，一般情况，把入口类放在 groupId + arctifactID 的 package 路径下。入口类的 mian 方法其实就是启动 Java 程序标准的 main 方法，```SpringApplication.run(BootApplication.class, args)``` 表示启动 Spring Boot 项目。

```java
@SpringBootApplication
public class BootApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }
}
```

```@SpringBootApplication``` 是 Spring Boot 的核心注解，可以写成如下三个注解，效果是等同的：

```java
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class BootApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }
}
```

```@Configuration``` 实际上是 ```@Component``` 的再封装，用于定义配置类，用于替换相应的 xml 配置文件，标识这个类可以用 Spring IOC 容器作为 Bean 定义的来源。被注解的类包含一个多个 ```@Bean``` 注解的方法，```@Bean``` 注解的方法将产生一个 Bean 对象，这些方法只会调用一次，然后由 Spring 放入 IOC 容器中。

```@ComponentScan``` 默认把当前 package 路径作为扫描路径，扫描并把标识了```@Controller，@Service，@Repository，@Component ``` 注解的类到 Spring 容器中。可以使用 ```@ComponentScan(value = "")``` 指定扫描路径。

```@EnableAutoConfiguration``` 让 Spring Boot 根据依赖自动为当前项目进行自动配置。例如添加了 mybatis-plus-boot-starter 这个依赖，Spring Boot 会自动进行相关配置。

## 2、实战 RESTful API

这一小节，将介绍实战如何构建 RESTful API 接口，如何返回和传入不同格式的数据。

### 2.1、 Hello Spring Boot

实现一个 API 接口，返回字符串 “ Hello Spring Boot”。

新建一个 HelloSpringBoot 的类，输入如下内容，然后再次运行 Spring Boot：

```java
@RestController
@RequestMapping("hello")
public class HelloSpringBoot {
    @RequestMapping("string")
    @ResponseStatus(HttpStatus.OK)
    public String helloString(){
        return "Hello Spring Boot";
    }
}
```

打开 Postman，访问 [http://localhost:8080/hello/string](http://localhost:8080/hello/string) ，可以看到返回了字符串 “ Hello Spring Boot”。

![Hello Spring Boot](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200202220726.png)

**代码解释** 这端代码。主要使用了两个注解和写了一个方法返回一个字符串。

```@RestController``` 是一个组合注解，等同于 ```@Controller``` 加 ```@ResponseBody``` 。```@Controller``` 声明这是一个控制器，注解的类可以接受 HTTP 请求，只使用该注解的话，视图解析器会解析 return 返回字符对应的 HTML 、JSP 页面等；

```@ResponseBody``` 会直接把 return 的内容写入 HTTP Response Body 中，视图解析器 InternalResourceViewResolver 将不起作用。

```@ResponseStatus(HttpStatus.XXX)``` 设置 HTTP Response 的状态码，默认 200。

```@RequestMapping``` 可以注解在类上，也可以注解在方法上。用于指定请求 URL 和对应处理方法的映射。 注解在类上的  ```@RequestMapping("hello")``` 表示这个类下的所有 URL 为 [/hello/](/hello/) 加 注解在方法上的 RequestMapping 的值。可以通过如下方法指定 HTTP 请求方法；如果指定请求方法 method 则必须使用改方法请求，否则报 "Request method 'XXX' not supported"。

```java
// 等同于  @GetMapping("string") 仅支持 GET 方法访问
@RequestMapping(value = "string",method = RequestMethod.GET)

// 等同于 @PostMapping("string") 仅支持 POST 方法访问
@RequestMapping(value = "string",method = RequestMethod.POST)
```

### 2.2 、返回 JSON 数据

上一个例子中，RESTful API 接口返回了字符串 String。对于 Spring Boot 来说，返回 JSON 数据也是相当简单的。这一小节将实现一个返回如下 JSON 数据的接口：

```json
{
    "status": "success",
    "messages": "Hello Spring Boot By JSON"
}
```

新建一个类 Messages：这里使用 lombok ，记得引入 'org.projectlombok:lombok' 这个依赖、 IDEA 勾选 “Enable annotation processing ” 和安装 lombok 插件。 

```java 
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Messages {
    private String  status;
    private String messages;
}
```

在 HelloSpringBoot 这个类新增方法 helloJson，然后重新运行项目：

```java
    @RequestMapping("json")
    public Messages helloJson(){
        return new Messages("success","Hello Spring Boot By JSON");
    }
```

通过 Postman 访问 [http://localhost:8080/hello/json](http://localhost:8080/hello/json) , 则返回了如上所示的 JSON 数据。

### 2.3 、传入数据 @RequestParam

```@RequestParam``` 是传入 [?name0=value0&name1=value1](?name0=value0&name1=value1) 这中类型参数的注解，用法如下：

```java
@RequestParam(value = "username",required = true,defaultValue = "Boot")
```

- value：参数名，可以不设置，默认跟方法函数的形参名相同
- required ：是否必须传入，默认 false ，设置为 true 则必须传入这个参数，否则报错
- defaultValue：默认值，如果设置了默认值，设置 required = true 的情况下不传入该参数不会报错

在 HelloSpringBoot 这个类新增方法 param ，然后重新运行项目：

```java
    @GetMapping(value = "param")
    public Messages param(@RequestParam(value = "username",defaultValue = "Boot") String username){
        return new Messages("success","Hello "+ username);
    }
```

通过 Postman 使用 GET 方法访问 [http://localhost:8080/hello/param?username=xiaoxian](http://localhost:8080/hello/param?username=xiaoxian) , 改变 username 参数的值，查看不同值返回的结果。

```json
{
    "status": "success",
    "messages": "Hello xiaoxian"
}
```

### 2.4 传入数据 @PathVariable

```@PathVariable``` 注解的作用是把 URL 路径的变量作为参数传入方法函数中。例如 GitHub 的个人主页URL  [https://github.com/zhshuixian](https://github.com/zhshuixian) 就是访问到小先的主页。[https://github.com/{username}](https://github.com/{username}) 中的 username 就是 *PathVariable*。

```
@PathVariable(value = "username" ,name = "username",required = true)
```

- value：PathVariable 名称，需要跟 RequestMapping 中括号括起来的一样 {  } 
- name：PathVariable 名称，跟 value 一样，两者仅需使用一个即可
- required：是否需要传入，默认 true

在 HelloSpringBoot 这个类新增方法 pathVariable，然后重新运行项目：

```java
    @PostMapping(value = "path/{username}")
    public Messages pathVariable(@PathVariable("username") String username){
        return new Messages("success","Hello "+ username);
    }
```

通过 Postman 使用 POST 方法访问 [http://localhost:8080/hello/path/username](http://localhost:8080/hello/path/username) 改变 {username} 的值，查看不同值返回的结果。

```json
{
    "status": "success",
    "messages": "Hello username"
}
```



### 2.5、传入数据 @RequestBody

```@RequestBody```  注解的作用是接收前端传入的 JSON 数据，假如我们需要提交如下数据:

```json
{
    "username": "user",
    "password": "springboot"
}
```

```java
@RequestBody(required = false) 
```

- required : 是否必须传入，默认为 true，设置 false 时候，如何没有传任何 json 数据，将不会 new 该对象，因此在使用对象的时候要**注意 Null PointerException**

新建一个 SysUser 的类:

```java
@AllArgsConstructor
@Getter
@Setter
public class SysUser {
    private String username;
    private String password;
}
```

在 HelloSpringBoot 这个类新增方法 body，然后重新运行项目：

```java
    @PostMapping("body")
    public Messages body(@RequestBody SysUser sysUser){
        Messages messages = new Messages();
        // 需要注意 Null PointerException
        if(sysUser.getUsername() !=null && sysUser.getPassword() !=null &&
                sysUser.getUsername().equals("user") && sysUser.getPassword().equals("springboot")){
            messages.setStatus("success");
            messages.setMessages("Login  Success");
        }else {
            messages.setStatus("error");
            messages.setMessages("Login  Error");
        }
        return messages;
    }
```

打开 Postman，使用 POST 方法访问 [http://localhost:8080/hello/body](http://localhost:8080/hello/body) 并传入 JSON 数据：

![Body JSON 数据接收](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200204000020.png)

通过示例可以看出 ```@RequestBody```  注解主要是接收传入的 JSON 数据，并根据JSON 数据的 key 名称和成员变量名称对应，然后将 value 值通过 Setter 或者 AllArgsConstructor 构造函数赋值给对应名称的成员变量。在 Getter 成员变量的时候一定要**注意 Null PointerException**。如果 ```@RequestBody```  注解的是 String，这会 Body 的内容当做字符串赋值：

```java
    @PostMapping("text")
    public String text(@RequestBody String  text){
        return text;
    }
```

代码运行效果如下：

![Text 类型](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200204004503.png)

### 2.6 传入数据 Form 表单

Form 表单有```application/x-www-form-urlencoded``` 、```multipart/form-data``` 和 ```text/plain``` 三种方式，其中前两种可以比较简单的在 Spring Boot 中使用，只需把上一小节的 body 函数中的 ```@RequestBody``` 删掉即可，同样在 Getter 的时候要**注意 Null PointerException**：

```java
    @PostMapping("form")
    public Messages form(SysUser sysUser){
        Messages messages = new Messages();
        if(sysUser.getUsername() !=null && sysUser.getPassword() !=null &&
                sysUser.getUsername().equals("user") && sysUser.getPassword().equals("springboot")){
            messages.setStatus("success");
            messages.setMessages("Login  Success");
        }else {
            messages.setStatus("error");
            messages.setMessages("Login  Error");
        }
        return messages;
    }
```

重启项目后，打开 Postman，使用 POST 方法访问 [http://localhost:8080/hello/form](http://localhost:8080/hello/form) 并传入 Form 表单数据，如下框起来的方式都可以使用：

![Form表单数据](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200204003346.png)



对于如何上传、下载文件，如何传入 XML 格式的数据，这里暂不做介绍。

## 3、Spring Boot 单元测试

单元测试作为开发过程中重要的一环，在于发现各小模块内部可能存在的错误，提高代码质量，这里将使用 JUnit 5，实战如何使用 ```MockMvc``` 和 ```TestRestTemplate``` 对刚刚构建的 RESTful API 进行单元测试。

Spring Boot 的 ```spring-boot-starter-test``` 支持 JUnit 4 和 JUnit 5 单元测试，如果你只想使用 JUnit 5 ，则应排除 JUnit 4 的依赖。

>  注： Spring Boot *2.2.3.RELEASE* 版本新建的时候默认排除了JUnit 4 的依赖。

**Gradle 排除 JUnit 4**

```json
    testImplementation('org.springframework.boot:spring-boot-starter-test') {
        exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
    }
```

**Maven 排除 JUnit 4**

```xml
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
```

### 3.1、新建 HelloSpringBootTest 测试类

鼠标光标放在需要新建测试类 HelloSpringBoot 的中，然后按下快捷键 *Alt + Enter* ，会弹出一个选项菜单，点击 *Create Test* ,IDEA 会自动在 test 目录下的同 package 包创建 相应的测试类  HelloSpringBootTest 。

![新建 HelloSpringBootTest](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200204223638.png)

选择 JUnit 5，勾选需要进行单元测试的函数：

![新建单元测试类](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200204231039.png)

打开 HelloSpringBootTest  ，新增如下几个注解：

```java
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = BootApplication.class)
@AutoConfigureMockMvc
class HelloSpringBootTest {
    // 中间部分省略
}
```

``@ExtendWith(SpringExtension.class)`` 是 Spring Boot 运行 JUnit 5 所需的注解，放在测试类名之前，用来确定这个类怎么运行的。在 Spring Boot 2.1.x 之前的版本需要此注解，之后的版本，如实战项目用了 2.2.3 这个版本，已经在 ```@SpringBootTest``` 中了，你可以将其删除。

```@SpringBootTest``` 是 1.4.0 开始引入的一个测试的注解，```SpringBootTest.WebEnvironment.RANDOM_PORT``` 表示项目运行使用随机的端口号，```classes = BootApplication.class``` Spring Boot 项目的启动类，以上两个参数都为非必须项。

```@AutoConfigureMockMvc``` 自动配置 MockMVC。

### 3.2、MockMVC 的使用

实战如何使用 MockMVC 进行 POST、GET 请求，并对 HTTP 返回的状态码和 content()  中的内容进行验证是否正确。

```java
    @Autowired private MockMvc mvc;
    
    @Test
    void testHelloString() throws Exception {
        // URL 和 验证返回内容的代码在 mvc.perform 中
        // andExpect 对返回结果进行验证，如果不正确将视为测试案例运行失败
        this.mvc.perform(get("/hello/string")).andExpect(status().isOk())
                .andExpect(content().string("Hello Spring Boot"));
    }

    @Test
    void testHelloJson() throws Exception {
        // content().json() 对返回的 JSON 数据进行验证
        this.mvc.perform(get("/hello/json")).andExpect(status().isOk())
                .andExpect(content().json("{'status':'success';'messages':'Hello Spring Boot By JSON'}"));
    }

    @Test
    void testPathVariable() throws Exception {
        this.mvc.perform(post("/hello/path/xiaoxian")).andExpect(status().isOk())
                .andExpect(content().json("{'status':'success';'messages':'Hello xiaoxian'}"));
    }
    
    @Test
    void testForm() throws Exception {
        // contentType 指定上传数据的类型
        // param(key,value) 参数的key和 value 值
        this.mvc.perform(post("/hello/form")
                .contentType("application/x-www-form-urlencoded")
                .param("username", "user")
                .param("password", "springboot"))
                .andExpect(status().isOk())
                .andExpect(content().json("{'status':'success';'messages':'Login  Success'}"));
    }
```

**代码解析**

```@Autowired``` 对类中的成员变量、方法和构造函数进行标注，完成自动装配。在本代码中，使用了此注解后，在使用 ```mvc``` 这个成员变量时，并不需要 new，交给 Spring 自动装配。有一个 required = true/false 的参数，默认为 true。

```@Test``` 注解是 JUnit 的注解，JUnit 5 没有参数可以设置，其注解的  ```void``` 类型的方法将会当做测试用例，在执行测试的时候才会运行。

### 3.3、TestRestTemplate

实战如何使用 TestRestTemplate进行 POST、GET 请求，上传 JSON、String 类型的数据，并接受返回的 JSON 数据和使用 assertThat 断言验证数据是否正确返回。

```Java
    @Test
    void testParam() {
    	// 接收 JSON 数据
        Messages messages = this.restTemplate.getForObject("/hello/param?username=xiaoxian", Messages.class);
        assertThat(messages.getMessages()).isEqualTo("Hello xiaoxian");
    }

    @Test
    void testBody() {
        SysUser sysUser = new SysUser("user", "springboot");
        // 上传 JSON 数据
        Messages messages = this.restTemplate.postForObject("/hello/body", sysUser, Messages.class);
        assertThat(messages.getStatus()).isEqualTo("success");
        assertThat(messages.getMessages()).isEqualTo("Login  Success");
    }

    @Test
    void testText() {
        String string = "Hi,Spring Boot";
        // 上传 String 字符串
        String result = this.restTemplate.postForObject("/hello/text", string, String.class);
        assertThat(result).isEqualTo(string);
    }
```

### 3.4、运行单元测试

在 IDEA 中，单元测试代码中会自动出现绿色三角形箭头，点击即自动运行单元测试。点击类名左边的小箭头、右键“Run .....”或者快捷键 “Ctrl + Shift + F10”，运行完成后会在下方显示测试通过的案例和失败的案例。

![运行单元测试](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200204235024.png)

## 4、设置端口号和 HTTPS

Spring Boot 项目在启动的时候默认端口号是 8080，我们可以通过修改 src/main/resources/application.properties 这个文件来自定义端口号和启用 HTTPS。

**设置端口号**

在 application.properties 文件新增如下配置：

```bash
# 自定义端口号
server.port=8000
```

对于 HTTPS 来说，在开发项目中你可以生成自签名证书文件，当然通过浏览器访问的话会提示你此连接不安全，以及在 Postman 中你要关闭验证 HTTPS 的功能，在部署的时候换成 CA 机构的 SSL 数字证书即可。

**生成自签名证书文件**

```bash
# keytool 是 JDK 提供的自签名证书生成工具，以下两个命令都可以用于生成证书
# keytool -genkeypair -keystore ssl.p12 -storetype pkcs12 -validity 365
> keytool -genkey -alias worktool -keyalg RSA -keystore ssl.keystore
Enter keystore password:xiaoxian
Re-enter new password:xiaoxian
What is your first and last name?
  [Unknown]:  org
What is the name of your organizational unit?
  [Unknown]:  boot
What is the name of your organization?
  [Unknown]:  boot
What is the name of your City or Locality?
  [Unknown]:  MZ
What is the name of your State or Province?
  [Unknown]:  GD
What is the two-letter country code for this unit?
  [Unknown]:  CN
Is CN=org, OU=boot, O=boot, L=MZ, ST=GD, C=CN correct?
  [no]:  y

Generating 2,048 bit RSA key pair and self-signed certificate (SHA256withRSA) with a validity of 90 days
        for: CN=org, OU=boot, O=boot, L=MZ, ST=GD, C=CN
```

**HTTPS 配置**

```bash
# 启用 HTTPS
server.ssl.enabled=true
server.ssl.protocol=TLS
server.ssl.key-store=classpath:ssl.keystore
# 密码是生成证书时候的输入的密码
server.ssl.key-store-password=xiaoxian
```

重新运行项目，打开浏览器访问 [https://localhost:8000/hello/string](https://localhost:8000/hello/string)。



## 5、打包成 Jar 文件

打开 IDEA 的 Gradle 的工具窗口，双击运行 bootJar：

![bootJar](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200206001315.png)

运行完成后会在 build/lib 文件夹生成一个 Jar 文件，拷贝到部署环境，运行如下命令即可部署运行：

```bash
java -jar ./boot-0.0.1-SNAPSHOT.jar
```



![jar 文件位置](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200206001544.png)



如果觉得文章不错，欢迎给 GitHub 仓库打个 Stars。
欢迎关注“编程技术进阶”或小先的博客，《Spring Boot 2.X 实战》的系列文章会首投于公众号。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200310234334126.png)