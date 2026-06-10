package com.faultticket.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {

    private Long id;
    private Long ticketId;
    private Long userId;
    private String username;
    private String realName;
    private String content;
    private LocalDateTime createTime;
}
