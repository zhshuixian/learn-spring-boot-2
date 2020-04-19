# Spring Boot 2.X 实战--RESTful API 全局异常处理

> 博客主页：https://me.csdn.net/u010974701
>
> 源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)

在上一节"Shiro (Token)登录和注册"中，主要介绍了 Spring Boot 整合 Shiro 实现 Token 的登录和认证，这一小节中，我们将实现 Spring Boot 的全局异常处理，将异常成封装统一样式的 JSON 返回前端。

小先有次在开发 React + Spring Boot 的应用的时候，因为没有加统一的异常处理，被 React 的 debug 搞得很崩溃。(对 React 不怎么熟悉)，后面才发现是异常返回的格式和状态码的问题。

对于 Spring Boot 的应用来说，全局异常处理是必要的，对于发生的异常按照统一风格的 JSON 样式返回给前端，方便前端对于 API 接口异常的处理和对问题的追踪。然鹅，Spring Boot 自带的错误异常处理不一定符合我们实际开发。

例如：我们在前面 *06-security-token* 项目中，登录成功返回的如下样式的 json：

```json
{
    "status": "SUCCESS",
    "message": "返回的 Token"
}
```

如果我们用户名或者密码错误的时候，则会返回如下样式的 json:

```json
{
    "timestamp": "2020-04-15T16:44:25.414+0000",
    "status": 403,
    "error": "Forbidden",
    "message": "Access Denied",
    "path": "/api/user/login"
}
```

显然对于我们前端调用 API 返回处理是极不方便的，如果我们所有的 @RestController 都自己加上 try-catch，捕捉异常和处理异常，例如参数检验的异常、密码错误等登录业务中的异常，会显得代码异常臃肿也花费了更多的时间。

```java
    // *06-security-token* org.xian.token.service.SysUserService
    public MyResponse login(final SysUser sysUser) {
        try {
            // 验证用户名和密码是否对的
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
```

## 1、Spring Boot 全局异常处理

**新建项目**，09-error-controller，只需要引入 web 依赖即可。

![](https://gitee.com//ylooq/image-repository/raw/master/image2020/20200417003511.png)

**新建项目相关的类**

新建 MyResponse，作为统一样式 JSON 的实体类

```java
public class MyResponse{
    private String status;
    private String message;
    // 省略 getter setter constructor
}
```

新建 User，传入参数的检验

```java
public class User {
    @NotEmpty(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{3,16}$", message = "用户名需3到16位的英文,数字")
    private String username;
    // 省略 getter setter constructor
}
```

新建 MyException，自定义异常

```java
public class MyException  extends Throwable{
    private final String status;
    private final String  message;

    public MyException(String  status,String message) {
        this.status=status;
       this.message=message;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

新建 ErrorController

```java
@RestController
public class ErrorController {

    @RequestMapping("/throw")
    public String myThrow() throws MyException {
        // Throw MyException
        throw new MyException("Error", "Throw MyException");
    }

    @PostMapping("/user")
    public String user(@RequestBody @Valid final User user) {
        // 参数检验
        return "参数检验通过";
    }
}
```

运行项目，分别访问不存在的路径和上述路径：例如用 Get 方法访问 /user，会返回如下：

```json
{
    "timestamp": "2020-04-16T16:43:41.123+0000",
    "status": 405,
    "error": "Method Not Allowed",
    "message": "Request method 'GET' not supported",
    "path": "/user"
}
```



**添加全局异常处理，@RestControllerAdvice**

新建 ErrorRestControllerAdvice

```java
@RestControllerAdvice
public class ErrorRestControllerAdvice {
    /** 全局异常捕捉处理 返回 401 状态 */
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MyResponse errorHandler(Exception ex) {
        return new MyResponse("ERROR", ex.getMessage());
    }

    /** 自定义异常捕获,返回 500 状态 */
    @ExceptionHandler(value = MyException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MyResponse myException(MyException e) {
        return new MyResponse(e.getStatus(), e.getMessage());
    }

    /** Http Method 异常 返回 405 */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public MyResponse httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return new MyResponse("ERROR", e.getMessage());
    }

    /** 404异常,返回 404 NOT_FOUND 异常 */
    @ExceptionHandler(value = NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public MyResponse noHandlerFoundException(NoHandlerFoundException e) {
        return new MyResponse("ERROR", "资源不存在");
    }

    /** RequestBody 为空时返回此错误提醒,返回400 bad Request */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MyResponse httpMessageNotReadableException() {
        return new MyResponse("ERROR", "请传入参数");
    }

    /** RequestBody某个必须输入的参数为空时 返回 400 Bad Request */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MyResponse methodDtoNotValidException(Exception ex) {
        MethodArgumentNotValidException c = (MethodArgumentNotValidException) ex;
        List<ObjectError> errors = c.getBindingResult().getAllErrors();
        StringBuffer errorMsg = new StringBuffer();
        errors.forEach(x -> errorMsg.append(x.getDefaultMessage()).append(" "));
        return new MyResponse("ERROR", errorMsg.toString());
    }
}
```

代码解析：

`@RestControllerAdvice`：等于 `@ResponseBody` + `@ControllerAdvice`，`@ControllerAdvice` 是 Spring 增强的 Controller 控制器，用于定义`@ExceptionHandler`，`@InitBinder`和`@ModelAttribute`方法

`@ExceptionHandler(value = Exception.class)` : 是 将 value 捕获到的异常交由其注解的方法处理。如果是注解在 `@Controller ` 中，仅仅会当前类中生效，而注解在 `@ControllerAdvice` 则是全局有效的。

`@ResponseStatus(HttpStatus.BAD_REQUEST)` : 设置该异常的状态返回码。

重新运行项目，分布访问不存在的路径，已经 /throw，/user，观察其前后的不同。例如通过 GET 访问 /user，则会返回如下：

```json
{
    "status": "ERROR",
    "message": "Request method 'GET' not supported"
}
```



> Spring Boot 的微信登录因为个人开发者无法申请，因此暂定不写了。下一篇计划是 Spring Boot 整合 Redis 缓存。


