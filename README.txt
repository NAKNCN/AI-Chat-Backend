# AI 对话助手后端系统 (aichat)

基于 Spring AI + Chroma 的 RAG 知识库问答系统，支持多轮对话、流式输出、知识库管理和 Docker 一键部署。

## 技术栈

| 分层 | 技术 |
|------|------|
| 基础框架 | Spring Boot 3.2.5 |
| ORM | MyBatis-Plus 3.5.10 |
| 认证授权 | Spring Security + JWT |
| AI 对话 | Spring AI Alibaba（通义千问） |
| 向量数据库 | Chroma 0.5.5 |
| HTTP 客户端 | OkHttp 4.12 |
| 流式输出 | Spring WebFlux + SSE |
| 数据库 | MySQL 8.0 |
| 接口文档 | SpringDoc OpenAPI (Swagger) |
| 部署 | Docker + Docker Compose |
| CI/CD | GitHub Actions |

## 核心功能

- **多轮对话**：携带最近 10 轮历史上下文
- **RAG 知识库问答**：基于 Chroma 向量检索，回答有据可依
- **SSE 流式输出**：打字机效果，提升体验
- **知识库管理**：在线增删查文档，支持 Markdown 上传
- **用户认证**：Spring Security + JWT 无状态认证
- **全局异常处理**：统一 JSON 错误返回
- **健康检查**：Spring Boot Actuator `/actuator/health`

## 快速启动

### 环境要求
- JDK 17+
- Docker Desktop

### 1. 打包项目
```bash
mvn clean package -DskipTests
