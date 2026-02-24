# 代码评审发现的问题

> 本文档包含对毕业设计项目的代码评审发现的问题。
> 更新时间：2026-02-19
> 适用对象：毕业设计项目评估

---

## 评审范围

| 目录 | 语言/框架 | 说明 |
|-----------|-------------------|-------------|
| `/home/rain/exam/backend` | Java/Spring Boot | 后端REST API服务、业务逻辑、数据访问 |
| `/home/rain/exam/database` | SQL | 数据库模式、迁移脚本、初始数据 |
| `/home/rain/exam/frontend` | Vue.js/TypeScript | 前端SPA、UI组件、API集成 |

---

## 执行摘要

**代码库分析：**
- 后端：约70个Java文件，5个测试文件
- 数据库：3个SQL初始化文件
- 前端：Vue.js/TypeScript应用

**毕业设计适用性说明：**
本项目整体架构合理，采用标准的Spring Boot分层架构。对于毕业设计而言，大部分问题属于优化建议而非核心缺陷。以下列出需要在答辩前优先处理的关键问题，其余可作为"未来工作"方向进行说明。

---

## 发现问题（按优先级排序）

### 🔴 高优先级 - 答辩前必须修复

---

#### [SECURITY] AES加密使用ECB模式（不安全）
**严重程度：** 高
**文件位置：** `backend/src/main/java/com/southcollege/exam/utils/AesUtil.java`

**问题描述：**
AesUtil使用了Hutool的SecureUtil.aes()，默认采用ECB模式。ECB（电子密码本）模式在加密多个数据块时不安全，因为它不使用IV（初始化向量），相同的明文块会产生相同的密文。此功能用于加密AI API密钥。

**影响：** 相同内容的API密钥将产生相同的密文，可能泄露密钥结构信息。

**修复建议：**
1. 改用AES-GCM模式（带认证功能）
2. 每次加密操作生成随机IV
3. 将IV与密文一起存储

---

#### [SECURITY] AES默认密钥硬编码
**严重程度：** 高
**文件位置：** `backend/src/main/java/com/southcollege/exam/utils/AesUtil.java:19`

**问题描述：**
密钥存在硬编码默认值"a1b2c3d4e5f6g7h8"。如果生产环境配置未覆盖，攻击者知道此默认值即可解密所有API密钥。

**影响：** 如果使用默认密钥，加密将完全失效。

**修复建议：**
1. 从配置文件或环境变量读取密钥
2. 如果检测到使用默认密钥，记录警告日志
3. 添加启动验证确保密钥不是默认值

---

#### [ARCHITECTURE] 考试超时检查存在竞态条件
**严重程度：** 高
**文件位置：** `backend/src/main/java/com/southcollege/exam/service/ExamService.java`

**问题描述：**
autoSaveExam方法加载会话后，基于已加载的startedAt检查超时，然后保存。学生可以通过保持浏览器连接活跃来无限延长考试时间，因为超时检查使用的是缓存的开始时间。

**影响：** 学生可能能够将考试时间延长到允许时长之外。

**修复建议：**
1. 使用数据库级时间戳比较进行超时检查
2. 添加数据库约束：`WHERE started_at + duration > NOW()`
3. 为会话更新实现乐观锁

---

### 🟡 中优先级 - 建议修复

---

#### [TESTING] 核心业务逻辑缺乏测试
**严重程度：** 中
**文件位置：** `backend/src/test/java/`

**问题描述：**
ExamService包含复杂的自动评分逻辑（JSON解析、类型特定处理、分数计算）。目前只有5个测试文件，主要覆盖工具类，缺少核心业务逻辑测试。

**缺失测试：**
- startExam流程
- submitExam评分逻辑
- 主观题评分

**修复建议：**
1. 为ExamService评分逻辑添加单元测试
2. 分别测试每种题型
3. 添加边界情况测试（空答案、无效JSON）

---

#### [CODE QUALITY] 静默异常捕获
**严重程度：** 中
**文件位置：** `backend/src/main/java/com/southcollege/exam/service/ExamService.java`

**问题描述：**
选择题答案检查的JSON解析异常被静默捕获，并回退到字符串比较。这可能掩盖数据损坏问题或答案格式的bug。

**修复建议：**
1. 在回退前记录警告级别的异常日志
2. 考虑在题目创建时进行更严格的验证

---

#### [Security] JWT令牌缺乏刷新机制
**严重程度：** 中
**文件位置：** `backend/src/main/java/com/southcollege/exam/utils/JwtUtil.java`

**问题描述：**
JWT令牌固定7天过期，没有刷新机制。用户将在7天后被强制登出且无任何警告。长期有效的令牌如果被泄露会增加安全风险。

**修复建议：**
1. 实现双token架构（访问令牌+刷新令牌）
2. 或者：在答辩时说明这是"未来改进方向"

**备注：** 此问题可在答辩时作为"未来工作"说明，非必须修复。

---

#### [Testing] SecurityUtil缺乏测试
**严重程度：** 中
**文件位置：** `backend/src/main/java/com/southcollege/exam/utils/SecurityUtil.java`

**问题描述：**
SecurityUtil类没有单元测试覆盖。此类可能包含需要充分测试的关键安全逻辑。

**修复建议：**
1. 创建SecurityUtilTest.java
2. 测试所有公共方法的各种输入场景
3. 包含边界情况测试

---

#### [Code Quality] ExamService.checkAnswer可能存在NPE
**严重程度：** 中
**文件位置：** `backend/src/main/java/com/southcollege/exam/service/ExamService.java`

**问题描述：**
checkAnswer方法可能未正确处理null情况。

**修复建议：**
1. 在处理答案检查前添加null检查
2. 在方法入口处验证输入参数

---

### 🟢 低优先级 - 可选改进

---

#### [ARCHITECTURE] ExamService违反单一职责原则
**严重程度：** 低
**文件位置：** `backend/src/main/java/com/southcollege/exam/service/ExamService.java`

**问题描述：**
ExamService类长达915行，包含多个职责：考试管理、评分、阅卷、会话处理和统计。这违反了单一职责原则。

**修复建议：**
可在答辩时说明这是"未来优化方向"，当前单体服务实现对于毕业设计是可接受的。

**未来优化方向：**
- 拆分为ExamManagementService（CRUD操作）
- ExamScoringService（自动评分逻辑）
- ExamSessionService（会话管理）

---

## 总结

### 问题统计

| 严重程度 | 数量 | 说明 |
|----------|-------|------------|
| 高 | 3 | 安全问题（2）、架构问题（1）|
| 中 | 6 | 测试（3）、代码质量（2）、安全（1）|
| 低 | 1 | 架构设计 |
| **总计** | **10** | |

### 答辩前建议

**必须修复（高优先级）：**
1. ✅ 修复AES加密模式 - 改用AES-GCM
2. ✅ 移除硬编码AES密钥 - 使用配置文件
3. ✅ 修复考试超时竞态条件 - 使用数据库时间戳

**建议修复（中优先级）：**
4. ⭕ 为核心评分逻辑添加单元测试
5. ⭕ 添加静默异常的日志记录
6. ⭕ 添加SecurityUtil测试

**可留作"未来工作"（低优先级）：**
- JWT刷新机制实现
- ExamService拆分重构
- 增加更多测试覆盖

### 项目优势

- ✅ 采用标准Spring Boot分层架构
- ✅ 关注点分离清晰
- ✅ 具备基础安全机制（加密、JWT、密码哈希）
- ✅ 数据库迁移脚本结构化

---

*本文档针对毕业设计项目进行了精简，移除了与评审过程相关的内容和生产环境优化建议。*
