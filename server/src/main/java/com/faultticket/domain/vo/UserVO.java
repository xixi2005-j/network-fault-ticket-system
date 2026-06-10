package com.faultticket.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer role;
    private Integer status;
    private LocalDateTime createTime;
}
