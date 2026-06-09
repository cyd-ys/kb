# RAG企业内部知识库问答系统

一个基于Spring Boot和Spring AI构建的RAG（检索增强生成）企业内部知识库问答系统。

## 🎯 项目概述

该系统实现了一个完整的RAG流程：
1. **文档上传** - 支持PDF、Word、Excel、TXT、Markdown等多种格式
2. **知识索引** - 智能分块、向量化存储
3. **语义检索** - 基于向量相似度的检索
4. **知识生成** - 通过LLM生成准确的答案

## 🏗️ 系统架构

```
┌──────────────────────────────────────┐
│   用户界面 (Vue 3)                    │
└────────────┬─────────────────────────┘
             │
┌────────────▼─────────────────────────┐
│  Spring Boot REST API (kb-server)     │
├───────────────────────────────────────┤
│  • 文档上传  • 文本提取  • 文本分块   │
│  • 向量生成  • 语义检索  • AI问答     │
└────────────┬─────────────────────────┘
             │
    ┌────────┴────────┬──────────┐
    │                 │          │
┌───▼────┐     ┌──────▼──┐  ┌───▼───┐
│ MySQL  │     │ Redis   │  │Milvus │
│ 元数据 │     │ 缓存    │  │向量库 │
└────────┘     └─────────┘  └───────┘
```

## 📋 技术栈

- **后端**: Spring Boot 3.0.0
- **AI框架**: Spring AI + OpenAI
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **向量库**: Milvus
- **前端**: Vue 3
- **文档处理**: Apache PDFBox、Apache POI

## 🚀 快速开始

### 前置要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0
- Redis 6.0+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/cyd-ys/kb.git
cd kb
```

2. **配置数据库**
编辑 `src/main/resources/application.yml`

3. **构建项目**
```bash
mvn clean install
```

4. **运行服务**
```bash
mvn spring-boot:run
```

## 📚 API文档

### 上传文档
```http
POST /api/knowledge/upload
Content-Type: multipart/form-data

file: <binary>
```

### 知识库查询
```http
POST /api/knowledge/query
Content-Type: application/json

{"question": "什么是RAG系统？"}
```

### 获取统计信息
```http
GET /api/knowledge/stats
```

## 🔑 主要功能

- ✅ 多格式文档上传和处理
- ✅ 智能文本分块和向量化
- ✅ 语义理解和检索
- ✅ LLM智能问答
- ✅ 用户认证授权
- ✅ 性能缓存优化

## 📝 许可证

MIT License

## 👥 联系方式

项目主页: https://github.com/cyd-ys/kb
