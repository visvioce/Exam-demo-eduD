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
            throw new BusinessException("配置不存在");
        }

        // 统一按明文处理并加密，避免将明文/掩码写回数据库
        if (entity.getApiKey() != null && !entity.getApiKey().isEmpty()) {
            encryptApiKey(entity);
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
        AiConfig config = getById(configId);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }

        if (!config.getModels().contains(model)) {
            throw new BusinessException("模型不存在于配置中");
        }

        // 使用 Mapper 批量清除该用户所有配置的激活模型（只更新 activeModel 字段）
        baseMapper.clearAllActiveModels(userId);

        // 设置新的激活模型
        config.setActiveModel(model);
        updateById(config);
    }

    /**
     * 加密 API Key
     */
    private void encryptApiKey(AiConfig config) {
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            config.setApiKey(AesUtil.encrypt(config.getApiKey()));
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
}
