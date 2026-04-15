package com.southcollege.exam.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页请求基类
 */
@Data
public class PageRequest {

    /**
     * 当前页码，默认 1
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer current = 1;

    /**
     * 每页大小，默认 10
     */
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 是否升序，默认降序
     */
    private Boolean asc = false;
}
