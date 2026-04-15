package com.southcollege.exam.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.util.Arrays;
import java.util.List;

/**
 * JSON 工具类
 * 
 * 数据格式规范：
 * - 正确答案格式：
 *   - 单选题：字符串，如 "A"
 *   - 多选题：字符串数组，如 ["A", "B", "C"]
 *   - 判断题：字符串，如 "正确" 或 "错误"
 *   - 填空题：单空为字符串，多空为字符串数组
 *   - 简答题：字符串
 * 
 * @see /docs/数据格式规范.md
 */
public class JsonUtil {

    /**
     * 将对象解析为字符串列表
     * 
     * 支持以下输入格式：
     * 1. List 类型（JacksonTypeHandler 从 JSON 数组反序列化）
     * 2. JSON 数组字符串，如 "[\"A\", \"B\"]"
     * 3. 分隔符分隔的字符串，如 "A,B,C" 或 "A\nB\nC"
     * 4. 单个字符串
     * 
     * @param obj 输入对象
     * @return 字符串列表，不为null
     */
    public static List<String> parseStringList(Object obj) {
        if (obj == null) {
            return List.of();
        }

        // 情况1：已经是 List 类型（JacksonTypeHandler 从 JSON 数组反序列化）
        if (obj instanceof List) {
            try {
			List<?> list = (List<?>) obj;
			return list.stream()
				.map(item -> item == null ? "" : item.toString())
				.filter(StrUtil::isNotBlank)
				.toList();
            } catch (Exception ignored) {
                // 转换失败，继续尝试其他方式
            }
        }

        // 转为字符串处理
        String raw = obj.toString();
        if (raw == null) {
            return List.of();
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return List.of();
        }

        // 情况2：JSON 数组字符串
        try {
            if (JSONUtil.isTypeJSONArray(value)) {
			List<String> arr = JSONUtil.toList(value, String.class);
			return arr == null ? List.of() : arr.stream().filter(StrUtil::isNotBlank).toList();
            }
        } catch (Exception ignored) {
            // JSON 解析失败走分隔符降级
        }

        // 情况3：分隔符分隔的字符串 - 增加黑名单检查，避免误拆分合法内容
        boolean hasSeparator = value.contains("\n") || value.contains(",") || value.contains("，")
                || value.contains(";") || value.contains("；") || value.contains("|");
                
        if (hasSeparator) {
            // 黑名单：如果是合法数值，不进行拆分
            boolean isNumeric = false;
            try {
                // 尝试解析为数值，支持千分位格式
                String normalized = value.replaceAll("[,，]", "");
                new java.math.BigDecimal(normalized);
                isNumeric = true;
            } catch (Exception ignored) {
            }
            
            // 黑名单：如果包含引号，可能是包含分隔符的完整字符串
            boolean hasQuotes = value.contains("\"") || value.contains("'") || value.contains("“") || value.contains("”");
            
            // 黑名单：如果包含小数点且逗号是千分位格式，不拆分
            boolean isThousandFormat = value.matches("^\\d{1,3}(,\\d{3})+(\\.\\d+)?$");
            
            // 只有在不是数值、不包含引号、不是千分位格式时才进行拆分
            if (!isNumeric && !hasQuotes && !isThousandFormat) {
	return Arrays.stream(value.split("[\\n,，;；|]+"))
				.map(String::trim)
				.filter(StrUtil::isNotBlank)
				.toList();
            }
        }

        // 情况4：单个字符串
        return List.of(value);
    }

    /**
     * 判断对象是否为有效的字符串列表（非空）
     * 
     * @param obj 输入对象
     * @return 是否为有效的非空字符串列表
     */
    public static boolean isNonEmptyStringList(Object obj) {
        if (obj == null) {
            return false;
        }
        
	// List 类型
	if (obj instanceof List) {
		List<?> list = (List<?>) obj;
		return list.stream().anyMatch(item -> item != null && StrUtil.isNotBlank(item.toString()));
	}
        
        // 字符串类型
        if (obj instanceof String) {
            String str = ((String) obj).trim();
            if (str.isEmpty()) {
                return false;
            }
            
            // 尝试解析为 JSON 数组
            try {
                if (JSONUtil.isTypeJSONArray(str)) {
                    List<String> arr = JSONUtil.toList(str, String.class);
		return arr != null && !arr.isEmpty() && arr.stream().anyMatch(StrUtil::isNotBlank);
                }
            } catch (Exception ignored) {
            }
            
            // 普通字符串
            return true;
        }
        
        return false;
    }
}
