package com.faultticket.domain.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ticket")
public class Ticket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    /** 分类：1-网络故障 2-设备故障 3-服务异常 4-其他 */
    private Integer category;

    /** 优先级：1-紧急 2-高 3-中 4-低 */
    private Integer priority;

    /** 状态：1-待处理 2-处理中 3-已完成 4-已关闭 */
    private Integer status;

    private Long creatorId;

    private Long assigneeId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private LocalDateTime resolveTime;
}
