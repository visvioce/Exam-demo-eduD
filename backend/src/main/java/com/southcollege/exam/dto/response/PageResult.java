package com.southcollege.exam.dto.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页响应结果
 */
@Data
public class PageResult<T> {

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 从 MyBatis-Plus 的 IPage 转换
     */
    public static <T> PageResult<T> from(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent((int) page.getCurrent());
        result.setSize((int) page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(page.getRecords());
        result.setHasNext(page.getCurrent() < page.getPages());
        result.setHasPrevious(page.getCurrent() > 1);
        return result;
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty(Integer current, Integer size) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(0L);
        result.setPages(0L);
        result.setRecords(List.of());
        result.setHasNext(false);
        result.setHasPrevious(current > 1);
        return result;
    }
}
