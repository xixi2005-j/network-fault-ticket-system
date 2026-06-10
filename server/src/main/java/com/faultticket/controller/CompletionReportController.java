package com.faultticket.controller;

import com.faultticket.common.Result;
import com.faultticket.domain.dto.CompletionReportDTO;
import com.faultticket.domain.vo.CompletionReportVO;
import com.faultticket.service.CompletionReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class CompletionReportController {

    private final CompletionReportService reportService;

    /**
     * 提交完成报告（运维人员）
     */
    @PostMapping("/ticket/{ticketId}")
    public Result<CompletionReportVO> submitReport(
            @PathVariable Long ticketId,
            @Valid @RequestBody CompletionReportDTO dto,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("报告提交成功", reportService.submitReport(ticketId, dto, userId));
    }

    /**
     * 获取工单的完成报告
     */
    @GetMapping("/ticket/{ticketId}")
    public Result<CompletionReportVO> getReport(@PathVariable Long ticketId) {
        return Result.success(reportService.getReportByTicketId(ticketId));
    }

    /**
     * 审核通过（管理员）
     */
    @PutMapping("/{reportId}/approve")
    public Result<CompletionReportVO> approve(
            @PathVariable Long reportId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("审核通过", reportService.approve(reportId, userId));
    }

    /**
     * 审核驳回（管理员）
     */
    @PutMapping("/{reportId}/reject")
    public Result<CompletionReportVO> reject(
            @PathVariable Long reportId,
            @RequestParam String rejectReason,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("已驳回", reportService.reject(reportId, rejectReason, userId));
    }
}
