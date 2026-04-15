package com.southcollege.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.southcollege.exam.entity.AiConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AiConfigMapper extends BaseMapper<AiConfig> {

    /**
     * 清除用户所有配置的激活模型
     */
    @Update("UPDATE user_ai_configs SET active_model = NULL WHERE user_id = #{userId}")
    int clearAllActiveModels(@Param("userId") Long userId);
}
