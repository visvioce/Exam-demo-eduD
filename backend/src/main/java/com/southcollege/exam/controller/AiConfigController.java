package com.southcollege.exam.controller;

import com.southcollege.exam.dto.response.Result;
import com.southcollege.exam.entity.AiConfig;
import com.southcollege.exam.service.AiConfigService;
import com.southcollege.exam.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI 配置管理", description = "AI API 配置增删改查")
@RestController
@RequestMapping("/api/ai-configs")
@com.southcollege.exam.annotation.RequireRole({com.southcollege.exam.enums.RoleEnum.ADMIN, com.southcollege.exam.enums.RoleEnum.TEACHER})
public class AiConfigController {

    @Autowired
    private AiConfigService aiConfigService;

    /**
     * 获取当前用户的AI配置列表
     */
    @GetMapping("/my")
    public Result<List<AiConfig>> getMyConfigs(HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        List<AiConfig> configs = aiConfigService.getByUserId(userId);
        // 脱敏 API Key
        configs.forEach(c -> c.setApiKey(maskApiKey(c.getApiKey())));
        return Result.success(configs);
    }

    /**
     * 获取当前用户的激活配置
     */
    @GetMapping("/my/active")
    public Result<AiConfig> getMyActiveConfig(HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        AiConfig config = aiConfigService.getActiveByUserId(userId);
        if (config != null) {
            config.setApiKey(maskApiKey(config.getApiKey()));
        }
        return Result.success(config);
    }

    /**
     * 获取单个配置详情（仅限自己的配置）
     */
    @GetMapping("/{id}")
    public Result<AiConfig> getById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        AiConfig config = aiConfigService.getById(id);
        if (config == null || !config.getUserId().equals(userId)) {
            return Result.error("无权访问此配置");
        }
        config.setApiKey(maskApiKey(config.getApiKey()));
        return Result.success(config);
    }

    /**
     * 创建AI配置
     */
    @PostMapping
    public Result<AiConfig> create(@RequestBody AiConfig aiConfig, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        aiConfig.setUserId(userId);

        // 输入验证
        String validationError = validateAiConfig(aiConfig);
        if (validationError != null) {
            return Result.error(validationError);
        }

        // 如果没有设置激活模型但有模型列表，默认激活第一个
        if ((aiConfig.getActiveModel() == null || aiConfig.getActiveModel().isEmpty())
                && aiConfig.getModels() != null && !aiConfig.getModels().isEmpty()) {
            aiConfig.setActiveModel(aiConfig.getModels().get(0));
        }

        aiConfigService.save(aiConfig);
        aiConfig.setApiKey(maskApiKey(aiConfig.getApiKey()));
        return Result.success(aiConfig);
    }

    /**
     * 更新AI配置（仅限自己的配置）
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody AiConfig aiConfig, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        AiConfig existing = aiConfigService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("无权修改此配置");
        }

        // 更新时允许不传 apiKey 或传掩码值，沿用原有密钥
        if (aiConfig.getApiKey() == null
                || aiConfig.getApiKey().trim().isEmpty()
                || aiConfig.getApiKey().startsWith("****")) {
            aiConfig.setApiKey(existing.getApiKey());
        }

        // 输入验证
        String validationError = validateAiConfig(aiConfig);
        if (validationError != null) {
            return Result.error(validationError);
        }

        aiConfig.setId(id);
        aiConfig.setUserId(userId);
        return Result.success(aiConfigService.updateById(aiConfig));
    }

    /**
     * 删除AI配置（仅限自己的配置）
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        AiConfig existing = aiConfigService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("无权删除此配置");
        }
        return Result.success(aiConfigService.removeById(id));
    }

    /**
     * 添加模型到配置
     */
    @PostMapping("/{id}/models")
    @Operation(summary = "添加模型", description = "向配置中添加一个新模型")
    public Result<AiConfig> addModel(
            @PathVariable Long id,
            @RequestBody AddModelRequest modelRequest,
            HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        AiConfig existing = aiConfigService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("无权操作此配置");
        }

        aiConfigService.addModel(id, modelRequest.getModel());
        AiConfig updated = aiConfigService.getById(id);
        updated.setApiKey(maskApiKey(updated.getApiKey()));
        return Result.success(updated);
    }

    /**
     * 删除配置中的模型
     */
    @DeleteMapping("/{id}/models/{model}")
    @Operation(summary = "删除模型", description = "从配置中删除指定模型")
    public Result<AiConfig> removeModel(
            @PathVariable Long id,
            @PathVariable String model,
            HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        AiConfig existing = aiConfigService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("无权操作此配置");
        }

        aiConfigService.removeModel(id, model);
        AiConfig updated = aiConfigService.getById(id);
        updated.setApiKey(maskApiKey(updated.getApiKey()));
        return Result.success(updated);
    }

    /**
     * 设置激活模型（通过URL路径传递模型名，兼容旧版调用）
     */
    @PostMapping("/{id}/models/{model}/activate")
    @Operation(summary = "激活模型(路径参数)", description = "将指定模型设为当前使用的模型（模型名通过URL路径传递）")
    public Result<AiConfig> activateModel(
            @PathVariable Long id,
            @PathVariable String model,
            HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        AiConfig existing = aiConfigService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("无权操作此配置");
        }

        aiConfigService.setActiveModel(id, model, userId);
        AiConfig updated = aiConfigService.getById(id);
        updated.setApiKey(maskApiKey(updated.getApiKey()));
        return Result.success(updated);
    }

    /**
     * 设置激活模型（推荐接口 — 通过请求体传递模型名，避免URL编码问题）
     */
    @PostMapping("/{id}/activate-model")
    @Operation(summary = "激活模型(请求体)", description = "将指定模型设为当前使用的模型（模型名通过请求体传递，推荐使用）")
    public Result<AiConfig> activateModelByBody(
            @PathVariable Long id,
            @RequestBody ActivateModelRequest body,
            HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        AiConfig existing = aiConfigService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("无权操作此配置");
        }
        if (body == null || body.getModel() == null || body.getModel().trim().isEmpty()) {
            return Result.error("模型名称不能为空");
        }

        aiConfigService.setActiveModel(id, body.getModel().trim(), userId);
        AiConfig updated = aiConfigService.getById(id);
        updated.setApiKey(maskApiKey(updated.getApiKey()));
        return Result.success(updated);
    }

    /**
     * 获取当前激活的模型信息（API Key已脱敏）
     * 注意：如需获取完整API Key用于后端调用，请使用 AiConfigService.getActiveByUserId()
     */
    @GetMapping("/active-model")
    @Operation(summary = "获取激活模型", description = "获取当前用户激活的模型信息（API Key已脱敏）")
    public Result<ActiveModelInfo> getActiveModel(HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        AiConfig config = aiConfigService.getActiveByUserId(userId);
        if (config == null || !config.hasActiveModel()) {
            return Result.success(null);
        }

        ActiveModelInfo info = new ActiveModelInfo();
        info.setConfigId(config.getId());
        info.setConfigName(config.getName());
        info.setBaseUrl(config.getBaseUrl());
        info.setApiKey(maskApiKey(config.getApiKey()));  // 脱敏处理，保护API Key安全
        info.setModel(config.getActiveModel());
        return Result.success(info);
    }

    /**
     * API Key 脱敏处理，只显示后4位
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 4) {
            return "****";
        }
        return "****" + apiKey.substring(apiKey.length() - 4);
    }

    /**
     * 验证 AI 配置输入
     * @return 错误信息，null 表示验证通过
     */
    private String validateAiConfig(AiConfig aiConfig) {
        // 验证配置名称
        if (aiConfig.getName() == null || aiConfig.getName().trim().isEmpty()) {
            return "配置名称不能为空";
        }
        if (aiConfig.getName().length() > 100) {
            return "配置名称不能超过100个字符";
        }

        // 验证 baseUrl
        if (aiConfig.getBaseUrl() == null || aiConfig.getBaseUrl().trim().isEmpty()) {
            return "API 地址不能为空";
        }
        if (!aiConfig.getBaseUrl().startsWith("http://") && !aiConfig.getBaseUrl().startsWith("https://")) {
            return "API 地址必须以 http:// 或 https:// 开头";
        }
        if (aiConfig.getBaseUrl().length() > 500) {
            return "API 地址不能超过500个字符";
        }

        // 验证 apiKey
        if (aiConfig.getApiKey() == null || aiConfig.getApiKey().trim().isEmpty()) {
            return "API Key 不能为空";
        }
        if (aiConfig.getApiKey().length() > 500) {
            return "API Key 不能超过500个字符";
        }

        // 验证模型列表
        if (aiConfig.getModels() != null) {
            for (String model : aiConfig.getModels()) {
                if (model == null || model.trim().isEmpty()) {
                    return "模型名称不能为空";
                }
                if (model.length() > 100) {
                    return "模型名称不能超过100个字符";
                }
            }
        }

        // 验证激活模型是否在模型列表中
        if (aiConfig.getActiveModel() != null && !aiConfig.getActiveModel().isEmpty()) {
            if (aiConfig.getModels() == null || !aiConfig.getModels().contains(aiConfig.getActiveModel())) {
                return "激活模型必须在模型列表中";
            }
        }

        return null;
    }

    /**
     * 添加模型请求
     */
    @lombok.Data
    public static class AddModelRequest {
        private String model;
    }

    /**
     * 激活模型请求（通过请求体传递）
     */
    @lombok.Data
    public static class ActivateModelRequest {
        private String model;
    }

    /**
     * 激活模型信息
     */
    @lombok.Data
    public static class ActiveModelInfo {
        private Long configId;
        private String configName;
        private String baseUrl;
        private String apiKey;
        private String model;
    }
}
