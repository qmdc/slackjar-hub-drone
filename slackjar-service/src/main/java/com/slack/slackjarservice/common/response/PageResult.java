package com.slack.slackjarservice.common.response;

import lombok.Data;

import java.util.List;

/**
 * 分页响应结果
 *
 * @param <T> 数据类型
 * @author zhn
 */
@Data
public class PageResult<T> {

    /**
     * 当前页码
     */
    private Integer pageNo;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 构造分页结果
     *
     * @param list 数据列表
     * @param total 总记录数
     * @param pageNo 当前页码
     * @param pageSize 每页大小
     */
    public PageResult(List<T> list, Long total, Integer pageNo, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 静态工厂方法
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer pageNo, Integer pageSize) {
        return new PageResult<>(list, total, pageNo, pageSize);
    }
}
