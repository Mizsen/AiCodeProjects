# Blog System 后端运行环境说明

## 1. 技术栈

- **Java 21**（推荐，Spring Boot 2.7.x 官方支持最佳）
- **Spring Boot 2.7.15**
- **Maven**（项目管理与依赖）
- **SQLite3**（数据库）
- **JPA/Hibernate**（ORM）
- **Log4j2**（日志）
- **Spring Security**（安全框架）
- **BCrypt**（密码加密）
- **自定义 Token 工具类**（用于登录认证，非标准 JWT）

## 2. 依赖管理

- 所有依赖通过 `pom.xml` 管理，主要依赖包括：
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-security`
  - `spring-boot-starter-log4j2`
  - `sqlite-jdbc`
  - `sqlite-dialect`
  - `spring-security-crypto`
  - `lombok`（可选，简化代码）
  - `spring-boot-starter-test`（测试）

> **注意**：`pom.xml` 中 `<java.version>` 推荐为 `17`，如需兼容 Java 21，需升级 Spring Boot 至 3.x 及相关依赖。

## 3. 配置文件

- `src/main/resources/application.properties` 主要配置项：
  - 端口：`server.port=8000`
  - 数据库：`spring.datasource.url=jdbc:sqlite:blog.db`
  - JPA 配置、日志级别等
  - Token 密钥：`jwt.secret=your_secret_key`

## 4. 日志

- 使用 Log4j2，日志配置见 `log4j2.xml`
- 支持控制台和文件日志，日志文件默认输出到 `logs/blog-app.log`

## 5. 运行方式

1. **环境准备**
   - 安装 JDK 17
   - 安装 Maven
   - 安装 SQLite3（如需手动管理数据库）

2. **编译与运行**
   ```bash
   mvn clean install
   mvn spring-boot:run



jpackage --type app-image --name myapp --input target
--main-jar T:\project\GithubProjects\nodejs-demo\qiye-project\202506091014\blog-system\backend\target\blog-1.0.0.jar --win-console --dest dist

