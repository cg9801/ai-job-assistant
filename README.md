# AI 求职助手 🎯

基于 Spring AI 的智能求职分析系统，通过 RAG（检索增强生成）实现简历与职位的语义匹配。

## 技术栈

- **Spring Boot 3.3** + **Spring AI 1.0**
- **向量数据库**：PgVector（PostgreSQL 扩展）
- **AI 能力**：Embedding + RAG + ChatClient
- **文档解析**：Apache Tika（支持 PDF/DOCX/TXT）
- **数据库**：H2（开发）/ PostgreSQL（生产）

## 项目结构

```
ai-job-assistant/
├── pom.xml
├── src/main/java/com/zhimai/jobassistant/
│   ├── AiJobAssistantApplication.java    # 启动类
│   ├── config/
│   │   ├── AiConfig.java                 # AI 配置
│   │   └── CorsConfig.java               # 跨域配置
│   ├── controller/
│   │   ├── ResumeController.java         # 简历 & 匹配接口
│   │   └── JobController.java            # 职位管理接口
│   ├── service/
│   │   ├── RagService.java               # ⭐ RAG 核心（检索 + 向量化）
│   │   ├── AiChatService.java            # ⭐ AI 对话（ChatClient 使用）
│   │   └── FileService.java              # 文件解析 & 技能提取
│   ├── model/
│   │   ├── JobPosting.java               # 职位实体
│   │   ├── Resume.java                   # 简历实体
│   │   ├── MatchResult.java              # 匹配结果
│   │   └── ApiResponse.java              # 统一响应
│   └── repository/
│       ├── JobPostingRepository.java
│       └── ResumeRepository.java
├── src/main/resources/
│   ├── application.yml                   # 配置文件
│   └── sample-jobs.json                  # 示例数据
└── README.md
```

## 核心流程

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐
│  上传简历    │────▶│  解析 & 存储  │────▶│  提取技能标签    │
└─────────────┘     └──────────────┘     └─────────────────┘

┌─────────────┐     ┌──────────────┐     ┌─────────────────┐
│  导入 JD    │────▶│  Embedding   │────▶│  存入向量数据库  │
└─────────────┘     └──────────────┘     └─────────────────┘

┌─────────────┐     ┌──────────────┐     ┌─────────────────┐
│  发起匹配    │────▶│  RAG 语义检索 │────▶│  LLM 分析 & 建议│
└─────────────┘     └──────────────┘     └─────────────────┘
```

## API 接口

### 简历操作
```bash
# 上传简历
curl -X POST "http://localhost:8080/api/resume/upload?username=zhimai" \
  -F "file=@resume.pdf"

# 匹配职位（RAG 检索）
curl "http://localhost:8080/api/match?username=zhimai&topK=5"

# 深度分析（简历 vs 具体职位）
curl "http://localhost:8080/api/analyze?username=zhimai&jobId=1"

# AI 问答
curl -X POST "http://localhost:8080/api/chat?username=zhimai&question=我适合投什么岗位"
```

### 职位管理
```bash
# 获取所有职位
curl "http://localhost:8080/api/jobs"

# 搜索职位
curl "http://localhost:8080/api/jobs/search?keyword=Java"

# 批量导入
curl -X POST "http://localhost:8080/api/jobs/batch" \
  -H "Content-Type: application/json" \
  -d @src/main/resources/sample-jobs.json
```

## 快速启动

### 1. 配置 API Key
```bash
export OPENAI_API_KEY=your-key-here
# 如果用国内代理：
export OPENAI_BASE_URL=https://api.your-proxy.com
```

### 2. 启动向量数据库（PgVector）
```bash
docker run -d --name pgvector \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  pgvector/pgvector:pg16
```

### 3. 运行项目
```bash
mvn spring-boot:run
```

### 4. 导入示例数据
```bash
curl -X POST "http://localhost:8080/api/jobs/batch" \
  -H "Content-Type: application/json" \
  -d @src/main/resources/sample-jobs.json
```

## 学习路线建议

1. **先跑通** → 上传简历 + 导入 JD + 匹配，看效果
2. **读代码** → 重点看 `RagService` 和 `AiChatService`
3. **改配置** → 换模型、调参数、试不同 Embedding
4. **加功能** → 面试题预测、简历优化、多轮对话
5. **做前端** → Vue/React 写个界面

## 可扩展方向

- [ ] 接入爬虫自动抓取 JD
- [ ] 支持多模型切换（通义千问、文心一言）
- [ ] 面试题预测 & 模拟面试
- [ ] 简历优化建议（针对具体岗位）
- [ ] 学习路线推荐
- [ ] 岗位趋势分析图表
