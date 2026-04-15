package com.southcollege.exam.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southcollege.exam.entity.AiConfig;
import com.southcollege.exam.exception.BusinessException;
import com.southcollege.exam.mapper.AiConfigMapper;
import com.southcollege.exam.utils.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiConfigService extends ServiceImpl<AiConfigMapper, AiConfig> {

    /**
     * 获取用户的所有 AI 配置（解密 API Key）
     * 使用 lambdaQuery 确保 TypeHandler 正确处理 models 字段
     */
    public List<AiConfig> getByUserId(Long userId) {
        List<AiConfig> configs = lambdaQuery()
                .eq(AiConfig::getUserId, userId)
                .list();
        // 解密 API Key
        configs.forEach(this::decryptApiKey);
        return configs;
    }

    /**
     * 获取用户的激活配置（有激活模型的配置）
     */
    public AiConfig getActiveByUserId(Long userId) {
        List<AiConfig> configs = getByUserId(userId);
        // 找到有激活模型的配置
        return configs.stream()
                .filter(AiConfig::hasActiveModel)
                .findFirst()
                .orElse(null);
    }

    /**
     * 保存 AI 配置（加密 API Key）
     */
    @Override
    @Transactional
    public boolean save(AiConfig entity) {
        encryptApiKey(entity);
        entity.setCreatedAt(LocalDateTime.now());
        return super.save(entity);
    }

    /**
     * 更新 AI 配置（加密 API Key）
     */
    @Override
    @Transactional
    public boolean updateById(AiConfig entity) {
        AiConfig existing = super.getById(entity.getId());
        if (existing == null) {
            throw new BusinessException("配置不存在，id=" + entity.getId());
        }

        String entityApiKey = entity.getApiKey();
        if (entityApiKey != null && !entityApiKey.isEmpty()) {
            if (isEncryptedApiKey(entityApiKey)) {
                log.warn("检测到传入的API Key已为加密状态（可能是解密失败残留），跳过重新加密，configId={}", entity.getId());
                entity.setApiKey(entityApiKey);
            } else {
                encryptApiKey(entity);
            }
        } else {
            entity.setApiKey(existing.getApiKey());
        }

        return super.updateById(entity);
    }

    /**
     * 根据 ID 获取配置（解密 API Key）
     */
    @Override
    public AiConfig getById(Serializable id) {
        AiConfig config = super.getById(id);
        if (config != null) {
            decryptApiKey(config);
        }
        return config;
    }

    /**
     * 添加模型到配置
     */
    @Transactional
    public void addModel(Long configId, String model) {
        AiConfig config = getById(configId);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }

        List<String> models = config.getModels();
        if (models == null) {
            models = new ArrayList<>();
        }

        if (models.contains(model)) {
            throw new BusinessException("模型已存在");
        }

        models.add(model);
        config.setModels(models);

        // 如果没有激活模型，设置第一个为激活
        if (config.getActiveModel() == null || config.getActiveModel().isEmpty()) {
            config.setActiveModel(model);
        }

        updateById(config);
    }

    /**
     * 从配置中删除模型
     */
    @Transactional
    public void removeModel(Long configId, String model) {
        AiConfig config = getById(configId);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }

        List<String> models = config.getModels();
        if (models == null || !models.contains(model)) {
            throw new BusinessException("模型不存在");
        }

        models.remove(model);
        config.setModels(models);

        // 如果删除的是激活模型，清除激活状态或激活第一个
        if (model.equals(config.getActiveModel())) {
            if (models.isEmpty()) {
                config.setActiveModel(null);
            } else {
                config.setActiveModel(models.get(0));
            }
        }

        updateById(config);
    }

    /**
     * 设置激活模型（全局唯一）
     * 优化：使用批量更新只修改 activeModel 字段，避免触发 API Key 加密逻辑
     */
    @Transactional
    public void setActiveModel(Long configId, String model, Long userId) {
        if (model == null || model.trim().isEmpty()) {
            throw new BusinessException("模型名称不能为空");
        }

        log.info("开始设置激活模型: configId={}, model={}, userId={}", configId, model, userId);

        AiConfig config = getById(configId);
        if (config == null) {
            throw new BusinessException("配置不存在，configId=" + configId);
        }

        List<String> models = config.getModels();
        if (models == null || models.isEmpty()) {
            throw new BusinessException("该配置没有模型列表，请先添加模型");
        }

        if (!models.contains(model)) {
            throw new BusinessException("模型 [" + model + "] 不存在于配置 [" + config.getName() + "] 的模型列表中，可用模型: " + String.join(", ", models));
        }

        try {
            baseMapper.clearAllActiveModels(userId);
        } catch (Exception e) {
            log.error("清除激活模型失败: userId={}", userId, e);
            throw new BusinessException("更新激活状态失败，请重试");
        }

        config.setActiveModel(model);

        try {
            updateById(config);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新配置失败: configId={}, model={}", configId, model, e);
            throw new BusinessException("保存激活模型失败: " + e.getMessage());
        }

        log.info("成功设置激活模型: configId={}, model={}", configId, model);
    }

    /**
     * 加密 API Key
     */
    private void encryptApiKey(AiConfig config) {
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            try {
                config.setApiKey(AesUtil.encrypt(config.getApiKey()));
            } catch (Exception e) {
                log.error("API Key 加密失败，configId={}", config.getId(), e);
                throw new BusinessException("API Key 加密失败，请检查系统配置");
            }
        }
    }

    /**
     * 解密 API Key
     */
    private void decryptApiKey(AiConfig config) {
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            return;
        }

        String apiKey = config.getApiKey();
        try {
            String decrypted = AesUtil.decrypt(apiKey);

            if (isValidApiKey(decrypted)) {
                config.setApiKey(decrypted);
                log.debug("API Key 解密成功，configId={}", config.getId());
            } else {
                log.warn("解密后的 API Key 格式异常，可能使用了不同的加密密钥，保持原值，configId={}", config.getId());
            }
        } catch (Exception e) {
            log.warn("API Key 解密失败，可能是明文存储，保持原值，configId={}, error={}",
                    config.getId(), e.getMessage());
        }
    }

    /**
     * 检查字符串是否像有效的 API Key
     */
    private boolean isValidApiKey(String key) {
        if (key == null || key.length() < 10 || key.length() > 200) {
            return false;
        }
        for (char c : key.toCharArray()) {
            if (c < 32 || c > 126) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断 API Key 是否已经处于加密状态（十六进制编码的密文）
     * 加密后的特征：长度为偶数、仅含十六进制字符、长度>=32
     */
    private boolean isEncryptedApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 32) {
            return false;
        }
        if (apiKey.length() % 2 != 0) {
            return false;
        }
        return apiKey.matches("^[0-9a-fA-F]+$");
    }
}
