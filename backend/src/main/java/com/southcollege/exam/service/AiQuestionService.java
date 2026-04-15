package com.southcollege.exam.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.southcollege.exam.dto.response.GeneratedQuestionResponse;
import com.southcollege.exam.entity.AiConfig;
import com.southcollege.exam.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * AI出题服务
 * 负责调用AI接口生成题目，并解析返回结果为结构化数据
 */
@Slf4j
@Service
public class AiQuestionService {

    @Autowired
    private AiConfigService aiConfigService;

    /**
     * 生成题目
     * @param userId 用户ID，用于获取AI配置
     * @param subject 科目
     * @param type 题目类型
     * @param difficulty 难度
     * @param count 数量
     * @param requirements 额外要求
     * @return 生成的题目列表
     */
    public GeneratedQuestionResponse generateQuestions(Long userId, String subject, String type,
                                                       String difficulty, Integer count, String requirements) {
        // 获取用户启用的AI配置
        AiConfig config = aiConfigService.getActiveByUserId(userId);
        if (config == null || !config.hasActiveModel()) {
            throw new BusinessException("未配置AI或没有激活的模型");
        }

        // 构建提示词
        String prompt = buildPrompt(subject, type, difficulty, count, requirements);
        // 调用AI接口
        String aiResponse = callAiApi(config, prompt);
        // 解析AI返回的文本
        return parseAiResponse(aiResponse);
    }

    /**
     * 构建提示词
     * 指导AI按指定格式返回JSON数据
     */
    private String buildPrompt(String subject, String type, String difficulty, Integer count, String requirements) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请生成").append(count).append("道");
        if (difficulty != null) {
            prompt.append(difficulty).append("难度的");
        }
        prompt.append(subject).append("科目的");
        if (type != null) {
            prompt.append(type).append("类型");
        }
        prompt.append("题目。\n\n");
        
        if (requirements != null && !requirements.isEmpty()) {
            prompt.append("额外要求：").append(requirements).append("\n\n");
        }
        
        // 要求AI按JSON格式返回，便于后续解析
        prompt.append("请严格按照以下JSON格式返回，不要包含其他内容：\n");
        prompt.append("{\n");
        prompt.append("  \"questions\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"content\": \"题目内容\",\n");
        prompt.append("      \"type\": \"题目类型(SINGLE_CHOICE/MULTIPLE_CHOICE/TRUE_FALSE/FILL_BLANK/ESSAY)\",\n");
        prompt.append("      \"difficulty\": \"难度(EASY/MEDIUM/HARD)\",\n");
        prompt.append("      \"score\": 分值,\n");
        prompt.append("      \"options\": [\n");
        prompt.append("        {\"id\": \"A\", \"text\": \"选项内容\"}\n");
        prompt.append("      ],\n");
        prompt.append("      \"correctAnswer\": \"正确答案（多选题和填空题为字符串数组，其他类型为字符串）\",\n");
        prompt.append("      \"explanation\": \"答案解析\",\n");
        prompt.append("      \"scoringCriteria\": [\n");
        prompt.append("        {\"point\": \"评分点\", \"score\": 分值}\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}");
        
        return prompt.toString();
    }

    /**
     * 调用AI API
     * 使用OpenAI格式的API接口
     */
    private String callAiApi(AiConfig config, String prompt) {
        try {
            // 构建请求体
            JSONObject body = new JSONObject();
            body.set("model", config.getActiveModel());

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.set("role", "user");
            userMessage.set("content", prompt);
            messages.add(userMessage);
            body.set("messages", messages);

            // 获取API URL（智能处理base_url）
            String apiUrl = getApiUrl(config.getBaseUrl());

            // 发送HTTP请求
            try (HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .timeout(60000)  // 60秒超时
                    .execute()) {

                if (response.getStatus() != 200) {
                    throw new BusinessException("AI请求失败: " + response.body());
                }

                // 解析响应
                JSONObject result = new JSONObject(response.body());
                JSONArray choices = result.getJSONArray("choices");
                if (choices == null || choices.isEmpty()) {
                    throw new BusinessException("AI返回结果为空");
                }

                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject messageObj = firstChoice.getJSONObject("message");
                return messageObj.getStr("content");
            }

        } catch (Exception e) {
            log.error("AI调用失败", e);
            throw new BusinessException("AI调用失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取完整的API URL
     * 智能处理base_url，兼容两种格式：
     * 1. base_url = "https://api.example.com/v1" -> 拼接 "/chat/completions"
     * 2. base_url = "https://api.example.com/v1/chat/completions" -> 直接使用
     */
    private String getApiUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new BusinessException("AI配置的base_url为空");
        }
        
        // 去除末尾的斜杠
        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        
        // 如果已经包含 /chat/completions，直接返回
        if (url.endsWith("/chat/completions")) {
            return url;
        }
        
        // 否则拼接
        return url + "/chat/completions";
    }

    /**
     * 解析AI返回的文本
     * 提取JSON数据并转换为结构化对象
     */
    private GeneratedQuestionResponse parseAiResponse(String aiResponse) {
        try {
            // 清理可能的markdown代码块标记 - 使用正则表达式处理多代码块情况
            String jsonStr = aiResponse;
            
            // 使用正则表达式找到最外层的JSON代码块，兼容多个```标记情况
            java.util.regex.Pattern jsonPattern = java.util.regex.Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```", java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher jsonMatcher = jsonPattern.matcher(aiResponse);
            if (jsonMatcher.find()) {
                jsonStr = jsonMatcher.group(1);
            } else {
                // 匹配无标记的通用代码块
                java.util.regex.Pattern generalPattern = java.util.regex.Pattern.compile("```\\s*([\\s\\S]*?)\\s*```", java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher generalMatcher = generalPattern.matcher(aiResponse);
                if (generalMatcher.find()) {
                    jsonStr = generalMatcher.group(1);
                }
            }
            
            jsonStr = jsonStr.trim();
            
            // 额外清理：去除可能的前置/后置说明文字，找到第一个{和最后一个}
            int startIndex = jsonStr.indexOf('{');
            int endIndex = jsonStr.lastIndexOf('}');
            if (startIndex >= 0 && endIndex > startIndex) {
                jsonStr = jsonStr.substring(startIndex, endIndex + 1);
            }
            
            // 解析JSON
            JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
            JSONArray questionsArray = jsonObject.getJSONArray("questions");
            
            GeneratedQuestionResponse response = new GeneratedQuestionResponse();
            List<GeneratedQuestionResponse.QuestionItem> questions = new ArrayList<>();
            
            // 遍历题目数组
            for (int i = 0; i < questionsArray.size(); i++) {
                JSONObject qObj = questionsArray.getJSONObject(i);
                GeneratedQuestionResponse.QuestionItem item = new GeneratedQuestionResponse.QuestionItem();

                // 安全获取字段，避免字段缺失导致整个解析失败
                item.setContent(qObj.getStr("content", ""));
                item.setType(qObj.getStr("type", "SINGLE_CHOICE"));
                item.setDifficulty(qObj.getStr("difficulty", "MEDIUM"));
                item.setScore(qObj.getBigDecimal("score", BigDecimal.ONE));

                // 处理正确答案 - 支持字符串和数组（多选题、填空题）
                // 数据格式规范：
                // - 单选题：字符串，如 "A"
                // - 多选题：字符串数组，如 ["A", "B", "C"]
                // - 判断题：字符串，如 "正确" 或 "A"
                // - 填空题：单空为字符串，多空为字符串数组，如 "答案" 或 ["答案1", "答案2"]
                // - 简答题：字符串
                Object answerObj = qObj.get("correctAnswer");
                if (answerObj instanceof JSONArray) {
                    // 多选题/填空题（多空） - 答案是数组，保持数组格式
                    JSONArray arr = (JSONArray) answerObj;
                    List<String> answers = new ArrayList<>();
                    for (int j = 0; j < arr.size(); j++) {
                        answers.add(arr.getStr(j, ""));
                    }
                    // 直接设置List，不要转成JSON字符串，让Jackson自动序列化
                    item.setCorrectAnswer(answers);
                } else {
                    // 单选题/判断题/填空题（单空）/简答题 - 答案是字符串
                    item.setCorrectAnswer(qObj.getStr("correctAnswer", ""));
                }
                item.setExplanation(qObj.getStr("explanation", ""));

                // 解析选项数组
                JSONArray optionsArray = qObj.getJSONArray("options");
                if (optionsArray != null) {
                    List<GeneratedQuestionResponse.Option> options = new ArrayList<>();
                    for (int j = 0; j < optionsArray.size(); j++) {
                        JSONObject optObj = optionsArray.getJSONObject(j);
                        GeneratedQuestionResponse.Option option = new GeneratedQuestionResponse.Option();
                        option.setId(optObj.getStr("id", ""));
                        option.setText(optObj.getStr("text", ""));
                        options.add(option);
                    }
                    item.setOptions(options);
                }

                // 解析评分标准数组（简答题）
                JSONArray scoringArray = qObj.getJSONArray("scoringCriteria");
                if (scoringArray != null) {
                    List<GeneratedQuestionResponse.ScoringCriterion> criteria = new ArrayList<>();
                    for (int j = 0; j < scoringArray.size(); j++) {
                       JSONObject critObj = scoringArray.getJSONObject(j);
                        GeneratedQuestionResponse.ScoringCriterion criterion = new GeneratedQuestionResponse.ScoringCriterion();
                        criterion.setPoint(critObj.getStr("point", ""));
                        criterion.setScore(critObj.getBigDecimal("score", BigDecimal.ZERO));
                        criteria.add(criterion);
                    }
                    item.setScoringCriteria(criteria);
                }

                questions.add(item);
            }
            
            response.setQuestions(questions);
            return response;
            
        } catch (Exception e) {
            log.error("解析AI返回结果失败，原始响应: {}", aiResponse, e);
            throw new BusinessException("解析AI返回结果失败: " + e.getMessage());
        }
    }

    // 线程池配置常量
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 8;
    private static final int QUEUE_CAPACITY = 100;
    private static final long KEEP_ALIVE_TIME = 60L;
    
    // 使用固定配置的线程池，避免资源耗尽
    private final ExecutorService executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new java.util.concurrent.ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "ai-question-worker-" + counter++);
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.AbortPolicy()  // 队列满时直接拒绝，避免系统过载
    );
    
    /**
     * 应用关闭时优雅关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        log.info("关闭AI服务线程池...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 流式生成题目（SSE）
     * 实时推送AI生成的内容到前端
     */
    public SseEmitter generateQuestionsStream(Long userId, String subject, String type,
                                               String difficulty, Integer count, String requirements) {
        // 创建SSE发射器，设置5分钟超时
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        
        // 用于跟踪是否已完成，避免重复调用 complete
        // 使用AtomicBoolean保证线程可见性和原子性
        final java.util.concurrent.atomic.AtomicBoolean isCompleted = new java.util.concurrent.atomic.AtomicBoolean(false);

        executor.execute(() -> {
            try {
                // 获取用户启用的AI配置
                AiConfig config = aiConfigService.getActiveByUserId(userId);
                if (config == null || !config.hasActiveModel()) {
                    // 发送错误事件而不是抛出异常
                    safeSendEvent(emitter, "error", "{\"message\": \"未配置AI或没有激活的模型，请先在个人中心配置并激活AI模型\"}", isCompleted);
                    safeComplete(emitter, isCompleted);
                    return;
                }

                // 构建提示词
                String prompt = buildPrompt(subject, type, difficulty, count, requirements);
                log.info("开始流式生成题目，用户ID: {}, 提示词长度: {}", userId, prompt.length());
                
                // 发送开始事件
                safeSendEvent(emitter, "start", "{\"status\": \"started\"}", isCompleted);
                log.info("已发送start事件");

                // 流式调用AI API
                StringBuilder fullContent = new StringBuilder();
                streamCallAiApi(config, prompt, new StreamCallback() {
                    @Override
             public void onChunk(String chunk) {
                         if (isCompleted.get()) return;
                         try {
                             // 发送内容块
                             safeSendEvent(emitter, "chunk", "{\"content\": \"" + escapeJson(chunk) + "\"}", isCompleted);
                             fullContent.append(chunk);
                             log.debug("发送chunk事件，内容长度: {}", chunk.length());
                         } catch (Exception e) {
                             log.error("发送SSE消息失败", e);
                         }
                     }

                     @Override
                     public void onComplete(String fullResponse) {
                         if (isCompleted.get()) return;
                         try {
                             log.info("AI响应完成，总长度: {}", fullResponse.length());
                             // 打印前500个字符用于调试
                             log.info("AI响应内容预览: {}",
                                 fullResponse.length() > 500 ? fullResponse.substring(0, 500) : fullResponse);

                             // 检查响应是否为空
                             if (fullResponse == null || fullResponse.trim().isEmpty()) {
                                 String errorMsg = "AI返回空响应，请检查API Key是否有效或AI服务是否正常";
                                 log.error(errorMsg);
                                 safeSendEvent(emitter, "error", "{\"message\": \"" + escapeJson(errorMsg) + "\"}", isCompleted);
                                 safeCompleteWithError(emitter, new RuntimeException(errorMsg), isCompleted);
                                 return;
                             }

                             // 解析完整响应
                             GeneratedQuestionResponse response = parseAiResponse(fullResponse);
                             log.info("解析完成，题目数量: {}", response.getQuestions().size());

                             // 检查是否生成了题目
                             if (response.getQuestions() == null || response.getQuestions().isEmpty()) {
                                 String errorMsg = "AI未生成任何题目，请尝试调整提示词或重试";
                                 log.warn(errorMsg);
                                 safeSendEvent(emitter, "error", "{\"message\": \"" + escapeJson(errorMsg) + "\"}", isCompleted);
                                 safeCompleteWithError(emitter, new RuntimeException(errorMsg), isCompleted);
                                 return;
                             }

                             // 发送完成事件，包含解析后的题目
                             safeSendEvent(emitter, "complete", JSONUtil.toJsonStr(response), isCompleted);
                             safeComplete(emitter, isCompleted);
                         } catch (Exception e) {
                             log.error("解析AI响应失败", e);
                             safeSendEvent(emitter, "error", "{\"message\": \"解析AI响应失败: " + escapeJson(e.getMessage()) + "\"}", isCompleted);
                             safeCompleteWithError(emitter, e, isCompleted);
                         }
                     }

                     @Override
                     public void onError(String error) {
                         if (isCompleted.get()) return;
                         log.error("AI调用出错: {}", error);
                         safeSendEvent(emitter, "error", "{\"message\": \"" + escapeJson(error) + "\"}", isCompleted);
                         safeCompleteWithError(emitter, new RuntimeException(error), isCompleted);
                     }
                }, isCompleted);

            } catch (Exception e) {
                log.error("流式生成题目失败", e);
                safeSendEvent(emitter, "error", "{\"message\": \"" + escapeJson(e.getMessage()) + "\"}", isCompleted);
                safeCompleteWithError(emitter, e, isCompleted);
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            isCompleted.set(true);
            safeComplete(emitter, isCompleted);
        });

        emitter.onCompletion(() -> {
            log.info("SSE连接完成");
            isCompleted.set(true);
        });
        
        // 设置错误回调，处理客户端断开连接
        emitter.onError(e -> {
            log.warn("SSE连接错误: {}", e.getMessage());
            isCompleted.set(true);
        });

        return emitter;
    }
    
    /**
     * 安全发送SSE事件，避免在连接已关闭时发送
     */
    private void safeSendEvent(SseEmitter emitter, String eventName, String data, java.util.concurrent.atomic.AtomicBoolean isCompleted) {
        if (isCompleted.get()) {
            log.debug("连接已完成，跳过发送事件: {}", eventName);
            return;
        }
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (Exception e) {
            log.error("发送SSE事件失败: {}, 错误: {}", eventName, e.getMessage());
            isCompleted.set(true);
        }
    }
    
    /**
     * 安全完成SSE连接
     */
    private void safeComplete(SseEmitter emitter, java.util.concurrent.atomic.AtomicBoolean isCompleted) {
        if (isCompleted.getAndSet(true)) return;
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("完成SSE连接时出错: {}", e.getMessage());
        }
    }
    
    /**
     * 安全地以错误完成SSE连接
     */
    private void safeCompleteWithError(SseEmitter emitter, Exception ex, java.util.concurrent.atomic.AtomicBoolean isCompleted) {
        if (isCompleted.getAndSet(true)) return;
        try {
            emitter.completeWithError(ex);
        } catch (Exception e) {
            log.debug("以错误完成SSE连接时出错: {}", e.getMessage());
        }
    }

    /**
     * 流式回调接口
     */
    private interface StreamCallback {
        void onChunk(String chunk);
        void onComplete(String fullResponse);
        void onError(String error);
    }

    /**
     * 流式调用AI API
     */
    private void streamCallAiApi(AiConfig config, String prompt, StreamCallback callback, java.util.concurrent.atomic.AtomicBoolean isCompleted) {
        HttpURLConnection connection = null;
        try {
            // 构建请求体
            JSONObject body = new JSONObject();
            body.set("model", config.getActiveModel());
            body.set("stream", true);  // 开启流式模式

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.set("role", "user");
            userMessage.set("content", prompt);
            messages.add(userMessage);
            body.set("messages", messages);

            // 获取API URL（智能处理base_url）
            String apiUrl = getApiUrl(config.getBaseUrl());
            log.info("流式API请求URL: {}", apiUrl);

            // 创建HTTP连接
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(300000);  // 5分钟读取超时

            // 发送请求体
            try (java.io.OutputStream os = connection.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorResponse = "未知错误";
                try (InputStream errorStream = connection.getErrorStream()) {
                    if (errorStream != null) {
                        errorResponse = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))
                                .lines().reduce("", (a, b) -> a + b);
                    }
                } catch (Exception e) {
                    log.warn("读取错误响应失败: {}", e.getMessage());
                }
                callback.onError("AI请求失败 (HTTP " + responseCode + "): " + errorResponse);
                return;
            }

            // 读取流式响应
            StringBuilder fullContent = new StringBuilder();
            int lineCount = 0;
            int chunkCount = 0;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null && !isCompleted.get()) {  // 检查连接是否已关闭
                    lineCount++;
                    log.debug("收到SSE行 {}: {}", lineCount, line.length() > 200 ? line.substring(0, 200) + "..." : line);
                    
                    // 兼容两种格式：data:xxx 和 data: xxx
                    String data = null;
                    if (line.startsWith("data:")) {
                        data = line.substring(5);
                        // 如果 data: 后面有空格，去掉空格
                        if (data.startsWith(" ")) {
                            data = data.substring(1);
                        }
                    }
                    
                    if (data != null) {
                        if ("[DONE]".equals(data)) {
                            log.info("收到SSE结束标记，共处理 {} 行，{} 个有效chunk", lineCount, chunkCount);
                            break;
                        }
                        try {
                            JSONObject chunk = JSONUtil.parseObj(data);
                            JSONArray choices = chunk.getJSONArray("choices");
                            if (choices != null && !choices.isEmpty()) {
                                JSONObject firstChoice = choices.getJSONObject(0);
                                JSONObject delta = firstChoice.getJSONObject("delta");
                                if (delta != null && delta.containsKey("content")) {
                                    String content = delta.getStr("content");
                                    if (content != null) {
                                        chunkCount++;
                                        fullContent.append(content);
                                        callback.onChunk(content);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("解析SSE数据失败: {}", data);
                        }
                    }
                }
                
                // 如果是客户端断开连接，提前退出
                if (isCompleted.get()) {
                    log.info("客户端已断开连接，终止流式读取");
                    return;
                }
            }

            log.info("流式读取完成，共 {} 行，{} 个有效chunk，总内容长度: {}", lineCount, chunkCount, fullContent.length());
            callback.onComplete(fullContent.toString());

        } catch (Exception e) {
            log.error("流式调用AI API失败", e);
            callback.onError("AI调用失败: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 转义JSON字符串中的特殊字符
     * 使用JSONUtil确保正确转义
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        // 使用Hutool的JSON工具进行转义，确保正确处理所有特殊字符
        return JSONUtil.escape(str);
    }
}
