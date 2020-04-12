# Spring Boot  2.x 实战--日志打印与输出到文件

>我是小先，一个专注大数据、分布式技术的非斜杠青年，爱Coding，爱阅读、爱摄影，更爱生活！
>
>大数据小先博客主页：https://me.csdn.net/u010974701
>
>源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)
>

日志对于程序的重要性不言而喻，当程序运行出现问题的时候，我们可以通过日志快速的定位、分析问题。

在开发的时候，还可以通过 IDE 的调试功能或者 System.out 、System.err 来简单打印一下信息，用来定位 bug，但这种方式是在任何时候都不推荐的，在生产环境中，撇开 System.out 会影响系统性能不说，出现问题的时候连错误日志记录在哪里都难找。

作为企业应用开发来说，可能有不同的功能模块，不同的功能模块之间的相互调用可能存在许多问题，在开发和演示环境中，很多问题可能无法暴露出来。当某个功能发生异常了，然而你无法像开发环境一样一步一步调试来找出问题，这时候能帮到你的只有日志记录。

Java 发展至今，有着非常成熟的生态体系，有着非常多成熟的日志框架可以选择， 因此也不推荐你从零搭建日志框架。

这里将实战 Spring Boot 整合 Log4j2 与 Slf4j 实现日志打印和输出到文件。

## 1、Java 日志框架和日志门面

在 Java 生态中，日志方面的技术主要分为日志框架和日志门面两大方面。日志框架是日志功能的具体实现，日志门面在日志框架的上面再做一层封装，对应用程序屏蔽底层日志框架的实现及细节。这里简要介绍 Java 常用的日志框架和日志门面。

### 1.1、常用的日志框架

**java.util.logging :** 是从 JDK 1.4 开始引入的 Java 原生日志框架，定义了七个日志级别，分别是：SEVERE、WARNING、INFO、CONFIG、FINE、FINER、FINEST。

**Log4j ：** 是 Apache 的开源项目，出自 Ceki Gülcü 之手，我们可以灵活地控制日志的输出格式、控制日志输出到控制台还文件，而这些无需对代码进行更改，只需要简单地更改一下配置文件即可。同样定义了七个日志级别：OFF、FATAL、ERROR、WARN、INFO、DEBUG、TRACE。

**LogBack ：** 同样是出自  Ceki Gülcü 之手的成熟日志框架，可以看做是 Log4j  的改良加强版本。

**Log4j2 ：** 不仅仅是 Log4j  升级版本，从头到尾被重写了，性能有着极大的提升，感兴趣的读者可以自行搜索 Log4j2 和其他日志框架的性能评析。

### 1.2、日志门面

在阿里巴巴《Java开发手册》中，在日志规范的第一条中，就是禁止直接使用上面所列的日志框架的 API，而应该使用像 SLF4J 日志门面的 API。

![日志规范](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200212212945.png)

上面所列的几种常用的日志框架，不同的框架有着不同的 API，这大大增加了代码与日志框架的耦合性，当需要更换日志框架的时候，我们几乎要把日志框架相关的代码重新修改一遍。

为了解决这个问题，可以通过在程序和日志框架在搭一个中间层实现，这就是日志门面，通过日志门面屏蔽日志框架的具体实现，即使我们更改了底层日志框架，也无需对程序进行大修改，顶多就是改改一些配置即可。

日志门面并不涉及具体的日志实现，还需依赖 Log4j、Logback 等日志框架，它仅仅是对程序屏蔽了不同日志框架的 API 差异，降低程序和日志框架之间的耦合度。

 **SLF4J ：** Java 简易日志门面（Simple Logging Facade for Java）的缩写，也是出自 Log4j  、 LogBack  的作者Ceki Gülcü 之手。支持 Java Logging API、Log4j、logback等日志框架。根据作者的说法，SLF4J  效率更高，比起 Apache Commons Logging (JCL) 更简单、更稳定。

**Commons Logging ：** Apache Commons Logging (JCL) 是基于Java的日志记录程序。

![日志框架和日志门面的关系](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200212221645.png)

## 2、实战 Log4j2 与 SLF4J

对于 Java 程序来说，**Log4j + SLF4J** 这样的组合来进行日志输出是个不错的选择，通过日志门面屏蔽底层日志框架的差异，即使在日后更换日志框架也无需太多的成本。

前面提到，Log4j2  对比 Log4j 有着巨大的提升，因此这里选择 Log4j2 + SLF4J 的组合。实战如何控制日志的控制台输出和日志输出到文件中。

### 2.1、引用 Log4j2 依赖

Spring Boot 默认使用 Logback 作为日志框架，默认引入了 spring-boot-starter-logging 这个依赖，修改 build.gradle（pom.xml），添加 Log4j2  依赖和排除 spring-boot-starter-logging 。

**项目添加 Log4j2  依赖、排除 spring-boot-starter-logging**

**build.gradle**

```json
// 排除 spring-boot-starter-logging
configurations {
    compile.exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
    implementation.exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
    testImplementation.exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
}
dependencies {
    // 其他依赖省略
    // 引入 Log4j2
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-log4j2
    compile group: 'org.springframework.boot', name: 'spring-boot-starter-log4j2'
}
```

**pom.xml**

```xml
  <dependencies>
    <dependency>
    <!-- 排除 spring-boot-starter-logging -->
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-logging</artifactId>
      <exclusions>
        <exclusion>
          <groupId>*</groupId>
          <artifactId>*</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
    <!-- 其他依赖省略 -->
    <!-- 引入 Log4j2 https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-log4j2 -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-log4j2</artifactId>
    </dependency>
  </dependencies>

```

刷新项目，可以发现 spring-boot-starter-logging 已经被移除和新引入了 spring-boot-starter-log4j2 。

### 2.2、 Log4j2 的配置

Log4j2 默认配置文件是 *resources/log4j2-spring.xml* ，新建Log4j2 的配置文件，如果你使用其他文件名称，可以通过如下方式指定。

**application.properties**

```bash
# 指定 Log4j2 配置文件
logging.config=classpath:other-filename.xml
```

**Log4j2-spring.xml 配置模板**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- Log4j2 配置文件 参考 https://www.cnblogs.com/keeya/p/10101547.html  -->
<!--日志级别以及优先级排序: OFF > FATAL > ERROR > WARN > INFO > DEBUG > TRACE > ALL -->
<!-- monitorInterval=“N” 自动间隔 N 秒检测配置文件是否修改，有修改则自动重新加载配置 可以不设置  -->
<!-- status="warn" Log4j2 本身日志输出级别 可以不设置 -->
<configuration monitorInterval="30" status="warn">
  <!-- 变量配置 -->
  <Properties>
    <!-- 日志输出格式 -->
    <property name="LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} %highlight{%-5level} [%t] %highlight{%c{1.}.%M(%L)}: %msg%n"/>
    <!-- 日志输出到文件的路径和文件名 根据项目情况更改 value 值 -->
    <property name="LOG_FILE_PATH" value="logger"/>
    <property name="LOG_FILE_NAME" value="log4j2"/>
  </Properties>
  <!-- 定义 appenders -->
  <appenders>
    <!-- console 设定 控制台输出 -->
    <console name="Console" target="SYSTEM_OUT">
      <!-- 指定 输出格式 默认 %msg%n -->
      <PatternLayout pattern="${LOG_PATTERN}"/>
      <!-- onMatch="ACCEPT" 只输出 level 级别及级别优先级更高的 Log , onMismatch="DENY" 其他拒绝输出  -->
      <ThresholdFilter level="debug" onMatch="ACCEPT" onMismatch="DENY"/>
    </console>
    <!-- 将日志全部输出到 test.log,append="true" 表示重新运行时不删除日志 -->
    <File name="FileLog" fileName="${LOG_FILE_PATH}/test.log" append="true">
      <PatternLayout pattern="${LOG_PATTERN}"/>
    </File>
    <!-- RollingFile 滚动输出日志到文件 -->
    <!-- 输出 warn 及更高优先级的 log 到 LOG_FILE_PATH 目录下的 warn.log 文件  -->
    <!-- filePattern 指定 warn.log 文件大于 size 大小时候文件处理规则, %d 日期;%i 编号(最大为下方设置的 max 值) -->
    <RollingFile name="RollingFileWarn" fileName="${LOG_FILE_PATH}/warn.log"
                 filePattern="${LOG_FILE_PATH}/%d{yyyy-MM-dd}/WARN_${LOG_FILE_NAME}_%i.log.gz">
      <PatternLayout pattern="${LOG_PATTERN}" />
      <ThresholdFilter level="warn" onMatch="ACCEPT" onMismatch="DENY"/>
      <Policies>
        <!-- interval="N" ，N小时滚动一次，默认是1 hour-->
        <TimeBasedTriggeringPolicy interval="1"/>
        <!-- size="5MB" 指定日志输出文件大小，若大小超过size，则日志会自动存入按 filePattern 规则建立的文件夹下面并进行压缩 -->
        <SizeBasedTriggeringPolicy size="5MB"/>
      </Policies>
      <!-- DefaultRolloverStrategy 不设置的情况下，默认为最多同一文件夹下7个 filePattern 规矩建立的压缩文件,多于 max 的值将用新的文件覆盖就的压缩文件 -->
      <DefaultRolloverStrategy max="10"/>
    </RollingFile>
    <!-- 输出 error 及更高优先级的 log 到 LOG_FILE_PATH 目录下的 error.log 文件  -->
    <RollingFile name="RollingFileError" fileName="${LOG_FILE_PATH}/error.log"
                 filePattern="${LOG_FILE_PATH}/%d{yyyy-MM-dd}/ERROR_${LOG_FILE_NAME}_%i.log.gz">
      <PatternLayout pattern="${LOG_PATTERN}" />
      <ThresholdFilter level="error" onMatch="ACCEPT" onMismatch="DENY"/>
      <Policies>
        <TimeBasedTriggeringPolicy interval="1"/>
        <SizeBasedTriggeringPolicy size="5MB"/>
      </Policies>
      <DefaultRolloverStrategy max="10"/>
    </RollingFile>
    <!-- 输出 info 及更高优先级的 log 到 LOG_FILE_PATH 目录下的 info.log 文件  -->
    <RollingFile name="RollingFileInfo" fileName="${LOG_FILE_PATH}/info.log"
                 filePattern="${LOG_FILE_PATH}/%d{yyyy-MM-dd}/Info_${LOG_FILE_NAME}_%i.log.gz">
      <PatternLayout pattern="${LOG_PATTERN}"/>
      <ThresholdFilter level="info" onMatch="ACCEPT" onMismatch="DENY"/>
      <Policies>
        <TimeBasedTriggeringPolicy interval="1"/>
        <SizeBasedTriggeringPolicy size="5MB"/>
      </Policies>
      <DefaultRolloverStrategy max="10"/>
    </RollingFile>
  </appenders>

  <!-- 在 Loggers 引入 Appender 使其生效 -->
  <loggers>
    <!-- Logger 节点用来单独指定 package 包下的 class 的日志输出格式等信息 -->
    <logger  name="org.springframework" level="info" additivity="false">
      <!-- 指定 org.springframework 的 level 及更高优先级的日志只在控制台输出 -->
      <!-- additivity="false" 只在自定义的Appender中进行输出 -->
      <AppenderRef ref="Console"/>
    </logger >

    <Root level="info">
      <!-- 用来指定项目的 Root 日志规则，如果没有单独指定Logger，那么就会默认使用 Root 日志输出 -->
      <!-- AppenderRef 用来指定日志输出到哪个 Appender -->
      <AppenderRef ref="Console"/>
      <AppenderRef ref="FileLog"/>
      <AppenderRef ref="RollingFileInfo"/>
      <AppenderRef ref="RollingFileWarn"/>
      <AppenderRef ref="RollingFileError"/>
    </Root>
  </loggers>
</configuration>
```

### 2.3、示例代码

```java
public class HelloSpringBoot {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BootApplication.class);
    
    @RequestMapping("string")
    @ResponseStatus(HttpStatus.OK)
    public String helloString(){
        log.trace("trace");
        log.debug("debug");
        log.warn("warn");
        log.info("info");
        log.error("error");
        return "Hello Spring Boot";
    }
    // 其他代码省略
}
```

或者 @Slf4j

```java
@Slf4j
public class HelloSpringBoot {

    @RequestMapping("string")
    @ResponseStatus(HttpStatus.OK)
    public String helloString(){
        log.trace("trace");
        log.debug("debug");
        log.warn("warn");
        log.info("info");
        log.error("error");
        return "Hello Spring Boot";
    }
    // 其他代码省略
}
```

运行项目，通过访问 https://localhost:8000/hello/string 可以看到如下输出

```shell
2020-02-18 01:15:20.620 WARN  [https-jsse-nio-8000-exec-10] o.x.b.HelloSpringBoot.helloString(20): warn
2020-02-18 01:15:20.620 INFO  [https-jsse-nio-8000-exec-10] o.x.b.HelloSpringBoot.helloString(21): info
2020-02-18 01:15:20.621 ERROR [https-jsse-nio-8000-exec-10] o.x.b.HelloSpringBoot.helloString(22): error
```

### 2.4、日志配置详解

### 2.4.1、日志级别

> 如果日志优先级等于或者大于配置的 level 就将其输出。如果设置为 Info，则 WARN 、ERROR 、FATAL  信息也会输出。而 DEBUG 、TRACE  则不会。
>
> **日志优先级排序** OFF > FATAL > ERROR > WARN > INFO > DEBUG > TRACE > ALL

- TRACE : 追踪，一般不使用，就是程序执行一步，就打个日志
- DEBUG : 调试信息，一般作为最低级别
- INFO : 重要的提示信息
- WARN : 警告信息
- ERROR : 错误信息
- FATAL : 致命错误信息 

#### 2.4.2、 配置文件

**根节点 Configuration** 有两个属性和两个子节点 **Appender** 和 **Loggers** 

- status 指定 Log4j2 本身日志输出级别
- monitorInterval=“N” 自动间隔 N 秒检测配置文件是否修改，有修改则自动重新加载配置

**Appender** 定义日志输出格式 

- Console : 输出到控制台
  - 属性 name :  Appender 的名称
  - 属性 target : SYSTEM_OUT 或 SYSTEM_ERR，一般设置为 SYSTEM_OUT
  - 子节点 PatternLayout : 日志输出格式
  - 子节点 ThresholdFilter : 日志输出级别 level
- File : 输出到文件
  - 属性 name :  Appender 的名称
  - 属性 fileName : 文件路径和名称
  - 属性 append : 重新运行项目是否保留先前的文件
  - 子节点 PatternLayout : 日志输出格式
- RollingFile : 滚动输出到文件，超过某个大小时候，自动覆盖旧日志，建议在线上项目使用
  - 属性 name :  Appender 的名称
  - 属性 fileName : 文件路径和名称
  - 属性 filePattern : fileName 文件超过指定 size 时候，归档文件存放目录和命名规则
  - 子节点 PatternLayout : 日志输出格式
  - 子节点 ThresholdFilter : 日志输出级别 level 
  - 子节点 Policies : 滚动输出策略
    - 子节点 : TimeBasedTriggeringPolicy 的 interval 属性用来指定多久滚动一次，单位小时
    - 子节点 : SizeBasedTriggeringPolicy 的 size 属性指定日志超过此大小按照 filePattern 压缩归档
  - 子节点 DefaultRolloverStrategy : max 属性值指定同一文件下 filePattern 压缩归档数量最大值。

**Loggers** 

- Logger : 单独指定某个 package 包下的 class 输出格式
  - 属性 name :  package 的名称
  - 属性 level：日志级别
  - 属性 additivity : 是否只在子节点 AppenderRef 里输出
  - 子节点 AppenderRef : 指定日志输出到那个 Appender
- Root：项目 Root 日志，如果没有使用 Logger  单独指定，则使用 Root 规则输出日志
  - 属性 level ：日志级别，如果 **Appender** 为指定，则使用这个
  - 子节点 AppenderRef : 指定日志输出到那个 Appender



**PatternLayout 自定义日志输出格式** 

```json
%d{yyyy-MM-dd HH:mm:ss.SSS}: 毫秒级别的日志产生时间
%highlight : 高亮显示
%-5level : 日志级别，-5表示左对齐并且固定输出5个字符，如果不足在右边补0
%c : 日志名称
%t : 线程名
%msg : 日志内容
%n : 换行符
%C : 类名
%L : 行数
%M : 方法名
%l : 包括类名、方法名、文件名、行数
```



**参考文档**

为什么阿里巴巴禁止工程师直接使用日志系统(Log4j、Logback)中的 API https://mp.weixin.qq.com/s/vCixKVXys5nTTcQQnzrs3w

https://www.cnblogs.com/keeya/p/10101547.html

SpringBoot整合log4j2日志全解 https://www.cnblogs.com/keeya/p/10101547.html



下一节，将实战 Spring Boot 2.X 连接 MySQL，分为 Spring Data JPA 和 MyBatis 两大块内容。更多内容，敬请关注《编程技术进阶》。
