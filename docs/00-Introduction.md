# Spring Boot  2.x 实战--什么是 Spring Boot？

> **Spring Boot**是 Pivotal 团队开发的、用于简化 Spring 应用的开发的开源框架。其通过自动配置和习惯优于配置的理念，使得 Spring Boot 应用程序几乎不需要繁琐的 Spring 配置，极大的提高开发效率。

源代码仓库：[https://github.com/zhshuixian/learn-spring-boot-2](https://github.com/zhshuixian/learn-spring-boot-2)

## 1、Spring Boot

Spring Boot 是快速创建、开发、运行 Spring 应用的开发框架，目的是让开发者尽可能地减少 Spring 应用的配置，更加注重实际业务代码的编写。

**Spring Boot 具有如下的特点**

- 无需太多的手动配置，开箱即用，能够快速创建、运行项目
- 内嵌 Tomcat 等，可以打包成 jar ，部署运行方便
- 为许多第三方开发库提供了几乎可以零配置的开箱即用的能力，如 Mybatis
- 提供了指标、健康检查和外部化配置等特性

### 1.1 、Spring Boot 2.X 新特性

Spring Boot 2.X 依赖于 Spring Framework  5 框架，因此需要 JDK 8 及以上的版本，同时对 Kotlin 语言有了更好的支持。

- 基于 Spring Framework  5，Spring 5 的新特性均可以使用
- 支持 Java 8+、Kotlin、Groovy
- 支持 Web Flux 和嵌入式 Netty Server 
- 支持各种组件响应式编程的自动化配置
- 升级了第三方依赖组件

### 1.2、Spring Boot 与 Spring 生态

**从根本上来讲，Spring Boot 不是对 Spring Framework 的增强和扩展，也不是用来替代 Spring 框架的**。其功能就是方便整合、管理 Spring 或者第三方组件，如 Spring Data JPA、Tomcat 、Alibaba Druid 等，实现框架的快速整合和自动配置。可以形象地比喻成**万能胶水**，你需要 Spring Data JPA 就把 JPA 粘上你的项目，你要用 Maybatis 就把Maybatis JPA 粘上你的项目即可，而你并不需要编写太多的整合代码或者配置，只需要**在依赖管理工具（Maven、Gradle）的配置文件写入相应的依赖即可**。

![Spring Boot 和 Spring](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200127114615.png)

## 2、搭建开发环境

### 2.1、OpenJDK

> Spring Boot 依赖于 JDK 8 及以上的版本，在本文中选择 OpenJDK 11。

从 OpenJDK 官网 [http://jdk.java.net/archive/](http://jdk.java.net/archive/)  下载对应系统的 OpenJDK 11，解压和设置 ```JAVA_HOME``` 指向你的安装目录和把 安装目录下的 bin 文件夹添加到 ```PATH```变量。目前最新的 OpenJDK 11 是  11.0.2 (build 11.0.2+9)。

![下载 OpenJDK 11](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200127114723.png)

### 2.2、IntelliJ IDEA Ultimate

IDEA 是功能强大的 Java 开发工具，虽然 IDEA Community 一样可以用于开发 Spring 应用，但由于没有原生支持 Spring 应用的开发，IDE 的配置和使用比较麻烦。这里推荐你使用 IDEA Ultimate 付费版，其对 Spring Boot 应用开发提供良好的支持。Ultimate  提供了 30 天免费试用，同时对教育、开源开发者提供了免费的授权。

IDEA 下载地址 [https://www.jetbrains.com/idea/](https://www.jetbrains.com/idea/) 

IntelliJ 教育授权计划  [https://www.jetbrains.com/zh/student/](https://www.jetbrains.com/zh/student/) (只要提供校园邮箱，国际学生证(ISIC)或是有效的证明文件来验证学生身份即可)

IntelliJ 开源项目申请免费授权 [https://www.jetbrains.com/shop/eform/opensource](https://www.jetbrains.com/shop/eform/opensource) (项目负责人或项目核心贡献者)

## 3、创建 Spring Boot 项目

> 本文示例项目选用 Gradle，如果你使用的是 Maven，你可以手工创建一个基于 Maven 的 Spring Boot 项目，把 pom.xml ,mvnw,mvnw.cmd,.mvn 这些文件拷贝到示例项目中，然后在 IDEA 初始化导入 Maven 项目，并把相关依赖包补齐。

#### 3.1、 创建项目

打开 IDEA，点击“Create New Project”创建新的项目：

![IDEA 启动页](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200127201627.png)

选择“Spring Initializr”，确保“Project SDK”要选择大于 JDK 8 的版本（点击“New”可以选择刚刚配置的 OpenJDK 11 的目录），点击“Next”：

![Spring Initializr](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200127202147.png)

“Type”选择 Gradle（如果你想使用 Maven 则更改即可，其他不变），“Language”选择 Java，“Packaging”打包方式选择 Jar，“Java Version” 需要和你选择的 JDK 版本一致，点击 Next：

![项目类型和语言等](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200127224712.png)

添加“Spring Web ”依赖，Spring Boot 版本在示例中选择 2.2.4，然后Next：

![Spring Web 依赖](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200127202849.png)

选择 Project 存储目录等，更改完成后直接 Finlsh 完成项目的创建，IDEA 会自动打开此项目：

![项目存储目录](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200127203134.png)

打开项目后，IDEA 的右下角会出现“Gradle/Maven projects need to be imported”提示，点击 “Enable Auto-Import 即可”：

![Import Project](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200127203348.png)

#### 3.2、配置国内镜像

Gradle 和Apache Maven 是自动化构建工具，用于管理项目的依赖、编译、打包、文档等信息。如果你想在项目添加其他依赖，可以在此网站 [https://mvnrepository.com/](https://mvnrepository.com/) 查询所依赖包的 Maven 或者 Gradle 的依赖配置信息，把它复制到 pom.xml 文件的```<dependencies></dependencies>``` 元素或者 build.gradle 文件的 ```dependencies { }``` 中即可。

IDEA 在创建项目的时候会自动下载相应的自动化构建软件和依赖，有时候由于网络原因，无法下载相关的资源等，需要手动下载和配置使用国内的镜像。

##### 3.2.1、Gradle 项目

Gradle 项目使用 build.gradle 文件管理项目依赖、编译和打包等信息。新建项目可能会遇到的问题有 Gradle 无法下载、相关依赖包无法下载。

**Gradle无法下载**：

先使用 IDEA 打开 gradle 项目，会自动在 ```GRADLE_USER_HOME``` 创建相应的存放目录，```GRADLE_USER_HOME``` 默认是用户个人目录下的 .gradle 文件夹。如果 Gradle 下载缓慢或者无法下载的时候，可以使用如下方式手动下载。

打开项目的 gradle/wrapper/gradle-wrapper.properties 文件，将 distributionUrl 的链接复制到浏览器下载：

```json
distributionUrl=https\://services.gradle.org/distributions/gradle-6.0.1-all.zip
```

以 gradle-6.0.1-all.zip 为例，将下载的文件复制如下目录，重新打开项目即可：

```bash
GRADLE_USER_HOME/wrapper/dists/gradle-6.0.1-all/99d3u8wxs16ndehh90lbbir67
```

**使用阿里云镜像**

修改项目的 build.gradle 文件，将 repositories 中的内容修改为如下，然后重新运行 Gradle：

```json
repositories {
    maven{ url 'http://maven.aliyun.com/nexus/content/groups/public/'}
    mavenCentral()
}
```

##### 3.2.2 Maven 项目

Maven 使用 pom.xml 来管理项目的依赖、编译和打包等信息。

**安装Maven** 下载地址 [http://maven.apache.org/download.cgi](http://maven.apache.org/download.cgi) ，你需要下载 Binary 二进制压缩包，解压到你的安装目录，并设置 ```M2_HOME``` 指向你的安装目录和把安装目录下的 bin 文件夹添加到 ```PATH``` 变量，配置完成后，可以在控制台输入命令查询是否安装成功：

```bash
mvn -v
```

打开 ```M2_HOME/conf/settings.xml``` ,将 ```<mirrors>``` 标签里的内容修改为如下：

```xml
  <mirrors>
        <mirror>
            <id>aliyun</id>
            <name>aliyun Maven</name>
            <mirrorOf>*</mirrorOf>
            <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
        </mirror>
  </mirrors>
```



![设置 Maven](https://gitee.com/ylooq/image-repository/raw/master/image2020/20200127222447.png)

**IDEA 设置 Maven home directory** 打开 IDEA 设置，“Build,Execution,Deployment”-->“Build Tools”-->“Maven”，将 “Maven home directory” 设置为你的 ```M2_HOME``` 目录，“User settings file”修改为刚刚修改的那个 settings.xml 文件。

本小节主要介绍了 Spring Boot 和如何创建 Spring Boot 的项目。下一节内容将介绍如何运行 Spring Boot 项目并编写一些简单的 RESTful API。



我是小先，一个专注大数据、分布式技术的非斜杠青年，爱Coding，爱阅读、爱摄影，更爱生活！