# Buff饰品交易平台

## 项目简介

本项目是一个仿网易BUFF的游戏虚拟饰品交易平台，基于C2C模式，专注于电商交易、订单流转、高并发场景下的库存与资金处理。

## 技术栈

- **框架**: Spring Boot 3.5.10
- **构建工具**: Maven
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **ORM**: MyBatis
- **认证**: JWT
- **API文档**: SpringDoc OpenAPI (Swagger)
- **文件存储**: 本地存储 / 阿里云OSS
- **日志框架**: Logback (SLF4J)

## 项目结构

```
src/main/java/com/buff/
├── common/              # 通用类
│   ├── Result.java      # 统一返回结果
│   ├── ResultCode.java  # 状态码枚举
│   └── PageResult.java  # 分页结果
├── config/              # 配置类
│   ├── JwtProperties.java
│   ├── FileProperties.java
│   ├── OssProperties.java
│   ├── RedisConfig.java
│   └── WebMvcConfig.java
├── controller/          # 控制器层
├── dto/                 # 数据传输对象
├── entity/              # 实体类
├── exception/           # 异常处理
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── interceptor/         # 拦截器
│   ├── AuthInterceptor.java
│   └── LogInterceptor.java
├── mapper/              # MyBatis Mapper接口
├── service/             # 服务层
│   └── impl/            # 服务实现
├── util/                # 工具类
│   ├── JwtUtils.java
│   ├── RedisUtils.java
│   ├── LogUtils.java    # 日志工具类
│   └── UserContext.java
└── vo/                  # 视图对象

src/main/resources/
├── application.yml      # 主配置文件
├── application-dev.yml  # 开发环境配置
├── application-prod.yml # 生产环境配置
├── logback-spring.xml   # 日志配置文件
└── mapper/              # MyBatis XML映射文件
```

## 核心功能模块

### 1. 用户认证与账户模块
- 注册/登录（JWT认证）
- Steam模拟绑定
- 个人中心（库存、出售、购买记录）

### 2. 资产与钱包模块
- 余额充值/提现
- 资金流水记录

### 3. 饰品市场模块
- 饰品字典管理
- 列表筛选（关键字、价格、磨损度）
- 商品详情页

### 4. 交易全流程模块
- 卖家上架/改价/下架
- 买家购买
- 订单状态机（待支付→待发货→已发货→交易完成）

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+

### 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE buff DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行SQL脚本：
```bash
mysql -u root -p buff < sql/buff.sql
```

### 配置文件

修改 `src/main/resources/application-dev.yml` 中的配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/buff
    username: root
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

### 启动项目

```bash
# 编译项目
mvn clean package

# 运行项目
mvn spring-boot:run

# 或者运行打包后的jar
java -jar target/Buff-0.0.1-SNAPSHOT.jar
```

### 访问应用

- 应用地址: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html
- API JSON: http://localhost:8080/v3/api-docs

## 核心配置说明

### 日志配置

项目已配置完善的日志系统，详细说明请参考：
- [日志系统配置说明](doc/日志系统配置说明.md)
- [日志使用指南](doc/日志使用指南.md)

**日志特性：**
- ✅ 控制台彩色输出
- ✅ 文件滚动记录（按日期和大小）
- ✅ 多环境配置（dev/test/prod）
- ✅ 异步日志（提升性能）
- ✅ 请求追踪（traceId）
- ✅ 用户追踪（userId）
- ✅ 日志归档和自动清理

**日志文件位置：**
- 开发环境: `./logs/`
- 生产环境: `/data/logs/buff/`

**日志文件类型：**
- `buff-all.log`: 所有级别的日志
- `buff-info.log`: INFO级别日志（仅生产环境）
- `buff-error.log`: ERROR级别日志

### JWT配置
```yaml
jwt:
  secret: your-secret-key        # JWT密钥（生产环境请使用环境变量）
  expiration: 7200000          # 过期时间（2小时）
```

### 文件上传配置
```yaml
file:
  upload:
    path: D:/WorkSpace/Projects/Buff/upload/buff/        # 本地存储路径
    base-url: http://localhost:8080/files/
```

### OSS配置
```yaml
oss:
  enabled: false                 # 是否启用OSS（false使用本地存储）
  endpoint: oss-cn-hangzhou.aliyuncs.com
  access-key-id: your-access-key-id
  access-key-secret: your-access-key-secret
  bucket-name: your-bucket-name
```

## 环境切换

通过修改 `application.yml` 中的 `spring.profiles.active` 来切换环境：

```yaml
spring:
  profiles:
    active: dev  # dev: 开发环境, prod: 生产环境
```

或者通过启动参数指定：
```bash
java -jar Buff-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 安全说明

### 生产环境部署注意事项

1. **敏感信息保护**：所有密码、密钥等敏感信息应通过环境变量注入
2. **JWT密钥**：使用强随机密钥，定期更换
3. **数据库连接**：使用连接池，配置合理的超时时间
4. **Redis密码**：生产环境必须设置Redis密码
5. **HTTPS**：生产环境必须使用HTTPS协议
6. **跨域配置**：根据实际需求限制允许的域名

## API认证

除了登录、注册等公开接口外，其他接口都需要在请求头中携带JWT令牌：

```
Authorization: Bearer <your-jwt-token>
```

## 并发安全

项目采用以下机制保证并发安全：

1. **乐观锁**：用户余额、商品库存使用version字段实现乐观锁
2. **数据库事务**：关键业务操作使用事务保证原子性
3. **Redis分布式锁**：可用于秒杀等高并发场景

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 所有公共方法必须添加JavaDoc注释

### 接口规范
- RESTful风格API设计
- 统一返回Result封装
- 统一异常处理

### 数据库规范
- 表名、字段名使用下划线命名
- 必须有主键和索引
- 关键字段添加注释

## 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 常见问题

### 1. 启动时连接数据库失败
- 检查MySQL是否启动
- 检查数据库连接配置是否正确
- 确认数据库已创建并执行了初始化脚本

### 2. Redis连接失败
- 检查Redis是否启动
- 检查Redis连接配置是否正确
- 确认Redis密码配置

### 3. JWT令牌验证失败
- 检查令牌是否过期
- 确认请求头格式：`Authorization: Bearer <token>`
- 检查JWT密钥配置是否一致

## 后续开发建议

1. **完善业务逻辑**：实现具体的业务Service和Controller
2. **添加单元测试**：为核心业务逻辑编写测试用例
3. **性能优化**：添加缓存策略、SQL优化
4. **监控告警**：集成监控系统（如Prometheus、Grafana）
5. **日志管理**：集成ELK日志分析系统

## 许可证

本项目仅供学习交流使用。

## 联系方式

如有问题，请提交Issue或联系开发团队。
