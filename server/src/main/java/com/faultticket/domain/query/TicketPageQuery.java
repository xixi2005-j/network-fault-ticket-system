package com.faultticket.domain.query;

import lombok.Data;

@Data
public class TicketPageQuery {

    private Integer page = 1;
    private Integer pageSize = 10;

    /** 关键词搜索（标题） */
    private String keyword;

    /** 状态筛选 */
    private Integer status;

    /** 优先级筛选 */
    private Integer priority;

    /** 分类筛选 */
    private Integer category;
}
