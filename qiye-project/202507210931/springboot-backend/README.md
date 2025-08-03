# Spring Boot 3 + Java 21 后端开发说明

## 1. 依赖建议
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- org.xerial:sqlite-jdbc

## 2. 配置 application.yml
```yaml
spring:
  datasource:
    url: jdbc:sqlite:../python-crawler/jobs.db
    driver-class-name: org.sqlite.JDBC
    username: ''
    password: ''
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
    database-platform: org.hibernate.dialect.SQLiteDialect
```

## 3. 用户模块（User）
- 实体类 User
- Repository、Service、Controller
- 支持注册、登录、用户信息管理

## 4. 岗位模块（Job）
- 实体类 Job（与 jobs.db 表结构一致）
- Repository、Service、Controller
- 支持岗位增删查改

## 5. 运行
- 用 IDE 或 `mvn spring-boot:run` 启动

---

详细代码见各模块目录。


sql

CREATE TABLE jobs (
    id          INTEGER   PRIMARY KEY AUTOINCREMENT,
    title       TEXT,
    company     TEXT,
    location    TEXT,
    salary      TEXT,
    experience  TEXT,
    education   TEXT,
    description TEXT,
    url         TEXT      UNIQUE,
    query_type  TEXT,
    city        TEXT,
    channel     TEXT,
    created_at TEXT
);


CREATE INDEX idx_jobs_title ON jobs(title);
CREATE INDEX idx_jobs_company ON jobs(company);
CREATE INDEX idx_jobs_city ON jobs(city);
CREATE INDEX idx_jobs_description ON jobs(description);
CREATE INDEX idx_jobs_created_at ON jobs(created_at);
CREATE INDEX idx_jobs_channel ON jobs(channel);
CREATE INDEX idx_jobs_query_type ON jobs(query_type);


CREATE TABLE users (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT,
    email    TEXT,
    password TEXT
);

CREATE UNIQUE INDEX idx_users_username ON users(username);