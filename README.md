# 高校实验室预约管理系统

基于 **Spring Boot 4.1.1 + Java 21 + Spring Data JPA + Thymeleaf + MySQL 8** 开发的高校实验室预约管理系统。系统采用 Maven 单模块工程，页面由 Thymeleaf 服务端渲染，围绕「实验室预约」核心业务提供学生、教师、管理员三类角色的完整功能，全部 CRUD 操作均可在页面完成并真实读写 MySQL 数据库。

## 一、技术栈

| 层次 | 技术选型 |
| --- | --- |
| 运行环境 | JDK 21、Maven 3.9+ |
| 后端框架 | Spring Boot 4.1.1、Spring MVC |
| 持久层 | Spring Data JPA（Repository + Specification + JPQL/原生 SQL）、Hibernate 7 |
| 数据库 | MySQL 8.0、HikariCP 连接池 |
| 页面层 | Thymeleaf 3.1 模板引擎、原生 CSS/JavaScript |
| 表单校验 | Jakarta Bean Validation（`@Valid` + `BindingResult`） |
| 其他 | Lombok、`@Scheduled` 定时任务、Session 拦截器鉴权 |

## 二、工程结构

```
实验室预约管理系统/
├── pom.xml                                 # Maven 依赖与构建配置
├── README.md
├── sql/
│   └── lab_reservation.sql                 # 建库建表 + 初始化演示数据
└── src/main/
    ├── java/com/campus/lab/
    │   ├── LabReservationApplication.java  # 启动类（含定时任务开关）
    │   ├── common/                         # 角色、状态常量、登录用户、分页工具、业务异常
    │   ├── config/                         # 拦截器注册、全局模型、Web 配置
    │   ├── controller/                     # 页面控制器与全局异常处理
    │   ├── entity/                         # JPA 实体：用户/实验室/预约/设备/审核记录/公告/配置
    │   ├── form/                           # 表单对象（登录、注册、预约、审核、用户、实验室等）
    │   ├── repository/                     # Spring Data JPA 数据访问接口
    │   ├── service/                        # 业务逻辑层
    │   ├── task/                           # 预约过期自动流转定时任务
    │   ├── util/                           # MD5 密码、日期格式化工具
    │   └── vo/                             # 时段视图对象
    └── resources/
        ├── application.yml                 # 端口、数据源、JPA、Thymeleaf 配置
        ├── messages.properties             # 表单类型转换中文提示
        ├── templates/                      # Thymeleaf 页面模板
        │   ├── fragments/common.html       # 页头、侧边导航、提示条公共片段
        │   ├── auth/                       # 登录、注册
        │   ├── student/                    # 学生首页、实验室详情、预约表单、我的预约
        │   ├── teacher/audit.html          # 教师审核
        │   ├── reservations/all.html      # 教师/管理员全部预约
        │   ├── reservation/detail.html     # 预约详情与审核时间线
        │   ├── admin/                      # 看板、实验室、用户、公告、规则
        │   ├── equipment/list.html         # 设备台账（学生只读 / 管理员维护）
        │   ├── profile.html                # 个人中心
        │   └── error.html                  # 统一错误页
        └── static/
            ├── css/app.css                 # 全局校园风样式
            └── js/app.js                   # 二次确认、时段选择、批量操作
```

## 三、环境要求

- JDK 21（`java -version` 需显示 21）
- Maven 3.9 及以上
- MySQL 8.0（服务已启动，默认端口 3306）

## 四、快速启动

### 1. 初始化数据库

使用命令行导入（会重建 `lab_reservation` 库，请勿在正式库执行）：

```bash
mysql -u root -p < sql/lab_reservation.sql
```

也可以使用 Navicat / DataGrip / MySQL Workbench 打开 `sql/lab_reservation.sql` 后整体执行。

脚本会创建 7 张表并写入演示数据：

| 表名 | 说明 |
| --- | --- |
| `sys_user` | 用户表（学生 / 教师 / 管理员） |
| `lab_info` | 实验室表（位置、容量、开放时段、状态） |
| `reservation` | 预约表（日期、起止时间、状态、审核信息） |
| `equipment` | 设备台账表 |
| `classroom` | 教学楼教室表（一号教学楼 5 层 × 20 间，编号 1101~1520） |
| `audit_record` | 预约审核记录表 |
| `system_config` | 预约规则配置表 |
| `announcement` | 系统公告表 |

### 2. 修改数据库连接

编辑 `src/main/resources/application.yml`，将账号密码改为本机 MySQL 的实际配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lab_reservation?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码
```

### 3. 启动系统

IDEA 方式：导入根目录 `pom.xml` 为 Maven 工程，确认 Project SDK 为 JDK 21，运行 `LabReservationApplication`。

命令行方式：

```bash
mvn spring-boot:run
```

或打包后运行：

```bash
mvn clean package -DskipTests
java -jar target/lab-reservation-system.jar
```

启动成功后访问：<http://localhost:8080>，未登录会自动跳转登录页。

## 五、演示账号

| 角色 | 账号 | 密码 | 说明 |
| --- | --- | --- | --- |
| 管理员 | admin | 123456 | 全部管理权限、数据看板 |
| 教师 | t001 / t002 | 123456 | 预约审核、查看全部预约 |
| 学生 | s001 / s002 / s003 | 123456 | 提交预约、查看与取消记录 |
| 学生（禁用） | s004 | 123456 | 用于验证禁用账号无法登录 |

> 密码以 MD5 存储，仅用于教学演示；生产环境请改用 BCrypt 并启用 HTTPS。

## 六、功能与页面

### 学生端

- 登录、注册、个人资料修改、密码修改
- 首页：系统公告、预约规则、开放实验室卡片
- 实验室详情：按日期查看开放时段（空闲 / 已占用 / 已结束），查看设备台账
- 提交预约：选择实验室、日期、起止时间、实验名称、用途、人数、备注；点击空闲时段可快速填入时间
- 我的预约：按状态、实验室、关键词、日期筛选；查看详情与审核时间线；取消未开始的预约
- 设备台账只读查询
- 空教室查询：一号教学楼 5 层共 100 间教室，按楼层 / 状态筛选，编号 1101~1520

### 教师端

- 待审核 / 已审核两个标签页，通过或驳回预约（驳回必填理由）
- 查看全部学生预约并按条件筛选
- 预约详情与审核记录
- 个人资料与密码修改

### 管理员端

- 数据看板：学生/教师/用户/实验室/设备/预约总量、今日预约、近 7 天趋势、状态分布、实验室排行、最新预约
- 实验室管理：新增、编辑、删除、开放时段与状态设置
- 用户管理：新增、编辑、启用/禁用、删除学生与教师账号
- 设备台账：新增、编辑、删除、维修登记、恢复正常、报废登记、多条件查询
- 教室管理：新增、编辑、删除教室，设置空闲 / 使用中 / 维护中状态
- 全部预约：多条件查询、单条强制取消、勾选批量强制取消
- 公告管理：发布、编辑、删除
- 预约规则：单次最长时长、最多提前预约天数、规则说明
- 个人中心

### 页面清单（共 11 类）

1. 登录 / 注册页　2. 学生首页　3. 实验室详情与时段页　4. 预约提交页　5. 我的预约页
6. 预约详情页　7. 教师审核页　8. 全部预约管理页　9. 设备台账页　10. 空教室查询 / 教室管理页　11. 管理员看板 / 用户 / 实验室 / 公告 / 规则 / 个人中心

## 七、核心业务规则

- 预约提交后状态为 `PENDING`（待审核）；教师审核后变为 `APPROVED` 或 `REJECTED`。
- 驳回必须填写理由，审核结果写入 `audit_record`；已审核预约不可重复审核（条件更新防并发）。
- 同一实验室、同一日期、时间重叠且状态为待审核/已通过时，禁止重复预约。
- 预约日期须在今天至「最多提前预约天数」范围内；时段须落在实验室开放时间内；人数不超过容量；时长不超过配置上限；已开始的时段不可预约。
- 学生可取消未开始且状态为待审核/已通过的预约；管理员可强制取消任意未结束预约，支持批量操作。
- 定时任务每分钟将结束时间已过的待审核/已通过预约自动置为 `EXPIRED`（已过期）。
- 鉴权：Session + 拦截器按 URL 前缀控制角色（`/admin/**` 管理员、`/teacher/**` 教师、`/student/**` 学生），未登录跳转登录页，越权返回 403；Controller 内对预约详情再次校验归属。

## 八、实现要点

- **分层结构**：Controller 负责路由与视图，Service 承载业务校验，Repository 负责数据访问，Entity 映射数据表，Form 负责表单接收与校验。
- **JPA 查询**：列表页使用 `JpaSpecificationExecutor` + `Specification` 组合状态、实验室、关键词、日期条件；冲突检测与过期流转使用 JPQL / 原生 SQL。
- **表单校验**：Jakarta Validation 注解配合 `BindingResult`，错误信息回显到页面字段下方；`LocalDate` / `LocalTime` 使用 `@DateTimeFormat` 绑定。
- **Thymeleaf**：`th:fragment` 公共页头与侧边栏、`th:field` 表单绑定、`#lists` / `#fields` 工具；登录用户与当前路径由 `@ControllerAdvice` 注入，避免使用已移除的 `${session}` 对象。
- **界面**：统一校园蓝绿配色、沉浸式登录页、卡片式列表、响应式布局，导航与操作按钮层级清晰。

## 九、常见问题

1. **启动报数据库连接失败**：确认 MySQL 已启动、`lab_reservation` 库已导入、`application.yml` 中账号密码正确；MySQL 8 的 URL 已附带 `allowPublicKeyRetrieval=true&useSSL=false`。
2. **页面中文乱码**：数据库需使用 `utf8mb4`，脚本已指定；连接 URL 已带 `characterEncoding=utf8`。
3. **登录提示账号或密码错误**：演示密码统一为 `123456`；`s004` 为禁用账号，无法登录。
4. **预约提交失败**：根据页面提示检查日期范围、开放时间、人数、时长与时段冲突。
5. **预约自动变为已过期**：定时任务每分钟执行一次，属于正常状态流转。
6. **项目路径包含中文**：Maven 与 Spring Boot 均支持，如个别环境异常可将目录改为英文后重新导入。
