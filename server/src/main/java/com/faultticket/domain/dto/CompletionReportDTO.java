package com.faultticket.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompletionReportDTO {

    @NotBlank(message = "完成工作内容不能为空")
    private String workDone;

    private String timeSpent;

    private String solution;
}
