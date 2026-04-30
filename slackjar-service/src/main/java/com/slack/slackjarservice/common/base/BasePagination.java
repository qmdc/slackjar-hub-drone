package com.slack.slackjarservice.common.base;

import lombok.Data;

/**
 * 分页参数DTO基类
 */
@Data
public class BasePagination {

    /**
     * 页码
     */
    private Integer pageNo = 0;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortBy = "createTime";

    /**
     * 排序方式(asc:升序, desc:降序)
     */
    private String sortOrder = "desc";

}
