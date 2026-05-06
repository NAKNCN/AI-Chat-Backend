项目简介：基于 Spring Boot 3.x 构建的 AI 对话助手后端服务。
技术栈：Spring Boot 3.2.5、MyBatis-Plus 3.5.10、MySQL 8.x、OkHttp、Jackson。
功能列表：
用户管理：增删改查、条件查询、分页查询。
AI 对话：调用通义千问 API，实现智能问答。
对话历史：自动保存问答记录，支持后续查询。
全局异常处理：统一返回 JSON 格式错误信息。
异步保存：使用 @Async 优化接口响应速度。
项目结构说明：简要描述各包（controller、service、mapper、pojo、config、exception）的职责。
如何运行：列出启动项目所需的步骤（配置数据库、修改 API Key、启动应用）。
接口列表：列出主要接口的 URL、方法和功能说明。