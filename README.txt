AI 对话助手后端系统 (aichat)
================================

基于 Spring AI + Chroma 的 RAG 知识库问答系统，支持多轮对话、流式输出、知识库管理和 Docker 一键部署。

技术栈
------
基础框架: Spring Boot 3.2.5
ORM: MyBatis-Plus 3.5.10
认证授权: Spring Security + JWT
AI 对话: Spring AI Alibaba（通义千问）
向量数据库: Chroma 0.5.5
HTTP 客户端: OkHttp 4.12
流式输出: Spring WebFlux + SSE
数据库: MySQL 8.0
接口文档: SpringDoc OpenAPI (Swagger)
部署: Docker + Docker Compose
CI/CD: GitHub Actions

核心功能
--------
- 多轮对话：携带最近 10 轮历史上下文
- RAG 知识库问答：基于 Chroma 向量检索，回答有据可依
- SSE 流式输出：打字机效果，提升体验
- 知识库管理：在线增删查文档，支持 Markdown 上传
- 用户认证：Spring Security + JWT 无状态认证
- 全局异常处理：统一 JSON 错误返回
- 健康检查：Spring Boot Actuator /actuator/health

快速启动
--------
环境要求: JDK 17+、Docker Desktop

1. 打包项目
   mvn clean package -DskipTests

2. 启动所有服务
   docker-compose up -d

3. 访问
   聊天界面: http://localhost:8081/index.html
   API 文档: http://localhost:8081/swagger-ui.html
   健康检查: http://localhost:8081/actuator/health

项目结构
--------
com.example.aichat
├── config          # 配置类（Chroma、MyBatis-Plus、Security）
├── controller      # 控制器（对话、知识库、文件上传、RAG）
├── exception       # 全局异常处理
├── mapper          # MyBatis-Plus Mapper 接口
├── pojo            # 实体类和 DTO
├── security        # JWT 过滤器和安全配置
├── service         # 业务服务接口与实现
└── vectorstore     # Chroma 向量存储自实现

接口概览
--------
方法    路径                         说明
POST    /auth/register               用户注册
POST    /auth/login                  用户登录
POST    /ai/chat                     多轮对话
POST    /chat/stream                 流式对话（SSE）
POST    /ai/rag                      RAG 知识库问答
GET     /chat/history                聊天历史
GET     /ai/knowledge/list           查看知识库
POST    /ai/knowledge/add            添加文档
DELETE  /ai/knowledge/delete/{id}    删除文档
POST    /ai/knowledge/upload         上传 Markdown

许可证
------
MIT License
