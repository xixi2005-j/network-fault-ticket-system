package com.faultticket.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompletionReportVO {

    private Long id;

    private Long ticketId;

    private Long reporterId;

    private String reporterName;

    private String workDone;

    private String timeSpent;

    private String solution;

    private String rejectReason;

    /** 状态：1-待审核 2-已通过 3-已驳回 */
    private Integer status;

    private String statusText;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
