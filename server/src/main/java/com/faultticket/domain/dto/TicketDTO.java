package com.faultticket.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "分类不能为空")
    private Integer category;

    private Integer priority;
}
