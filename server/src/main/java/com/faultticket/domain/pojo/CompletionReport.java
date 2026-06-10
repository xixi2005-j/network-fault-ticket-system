package com.faultticket.domain.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("completion_report")
public class CompletionReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ticketId;

    private Long reporterId;

    /** 完成的工作内容 */
    private String workDone;

    /** 耗时统计 */
    private String timeSpent;

    /** 解决方案描述 */
    private String solution;

    /** 驳回原因（管理员填写） */
    private String rejectReason;

    /** 状态：1-待审核 2-已通过 3-已驳回 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
