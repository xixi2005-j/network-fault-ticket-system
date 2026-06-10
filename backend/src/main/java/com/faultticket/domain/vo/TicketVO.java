package com.faultticket.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketVO {

    private Long id;
    private String title;
    private String description;
    private Integer category;
    private String categoryName;
    private Integer priority;
    private String priorityName;
    private Integer status;
    private String statusName;
    private Long creatorId;
    private String creatorName;
    private Long assigneeId;
    private String assigneeName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime resolveTime;
}
