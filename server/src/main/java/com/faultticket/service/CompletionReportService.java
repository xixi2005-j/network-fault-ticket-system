package com.faultticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.faultticket.common.BusinessException;
import com.faultticket.domain.dto.CompletionReportDTO;
import com.faultticket.domain.pojo.CompletionReport;
import com.faultticket.domain.pojo.Ticket;
import com.faultticket.domain.pojo.User;
import com.faultticket.domain.vo.CompletionReportVO;
import com.faultticket.mapper.CompletionReportMapper;
import com.faultticket.mapper.TicketMapper;
import com.faultticket.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompletionReportService {

    private final CompletionReportMapper reportMapper;
    private final TicketMapper ticketMapper;
    private final UserMapper userMapper;

    /**
     * 提交完成报告（运维人员）
     */
    @Transactional
    public CompletionReportVO submitReport(Long ticketId, CompletionReportDTO dto, Long reporterId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        // 只有处理人可以提交报告
        if (!ticket.getAssigneeId().equals(reporterId)) {
            throw new BusinessException(403, "只有处理人可以提交完成报告");
        }

        // 工单必须是处理中状态
        if (ticket.getStatus() != 2) {
            throw new BusinessException(400, "只有处理中的工单可以提交完成报告");
        }

        // 检查是否已有待审核的报告
        LambdaQueryWrapper<CompletionReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompletionReport::getTicketId, ticketId)
               .eq(CompletionReport::getStatus, 1);
        if (reportMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "已有待审核的报告，请等待审核");
        }

        // 创建完成报告
        CompletionReport report = new CompletionReport();
        report.setTicketId(ticketId);
        report.setReporterId(reporterId);
        report.setWorkDone(dto.getWorkDone());
        report.setTimeSpent(dto.getTimeSpent());
        report.setSolution(dto.getSolution());
        report.setStatus(1); // 待审核
        reportMapper.insert(report);

        // 更新工单状态为审核中
        ticket.setStatus(3);
        ticketMapper.updateById(ticket);

        return convertToVO(report);
    }

    /**
     * 获取工单的完成报告
     */
    public CompletionReportVO getReportByTicketId(Long ticketId) {
        LambdaQueryWrapper<CompletionReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompletionReport::getTicketId, ticketId)
               .orderByDesc(CompletionReport::getCreateTime)
               .last("LIMIT 1");
        CompletionReport report = reportMapper.selectOne(wrapper);
        if (report == null) {
            return null;
        }
        return convertToVO(report);
    }

    /**
     * 审核通过（管理员）
     */
    @Transactional
    public CompletionReportVO approve(Long reportId, Long adminId) {
        CompletionReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(404, "报告不存在");
        }

        if (report.getStatus() != 1) {
            throw new BusinessException(400, "该报告不在待审核状态");
        }

        // 更新报告状态
        report.setStatus(2); // 已通过
        reportMapper.updateById(report);

        // 更新工单状态为已完成
        Ticket ticket = ticketMapper.selectById(report.getTicketId());
        if (ticket != null) {
            ticket.setStatus(4); // 已完成
            ticket.setResolveTime(java.time.LocalDateTime.now());
            ticketMapper.updateById(ticket);
        }

        return convertToVO(report);
    }

    /**
     * 审核驳回（管理员）
     */
    @Transactional
    public CompletionReportVO reject(Long reportId, String rejectReason, Long adminId) {
        CompletionReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(404, "报告不存在");
        }

        if (report.getStatus() != 1) {
            throw new BusinessException(400, "该报告不在待审核状态");
        }

        // 更新报告状态
        report.setStatus(3); // 已驳回
        report.setRejectReason(rejectReason);
        reportMapper.updateById(report);

        // 更新工单状态回到处理中
        Ticket ticket = ticketMapper.selectById(report.getTicketId());
        if (ticket != null) {
            ticket.setStatus(2); // 处理中
            ticketMapper.updateById(ticket);
        }

        return convertToVO(report);
    }

    private CompletionReportVO convertToVO(CompletionReport report) {
        CompletionReportVO vo = new CompletionReportVO();
        vo.setId(report.getId());
        vo.setTicketId(report.getTicketId());
        vo.setReporterId(report.getReporterId());
        vo.setWorkDone(report.getWorkDone());
        vo.setTimeSpent(report.getTimeSpent());
        vo.setSolution(report.getSolution());
        vo.setRejectReason(report.getRejectReason());
        vo.setStatus(report.getStatus());
        vo.setCreateTime(report.getCreateTime());
        vo.setUpdateTime(report.getUpdateTime());

        // 查询报告人姓名
        User reporter = userMapper.selectById(report.getReporterId());
        if (reporter != null) {
            vo.setReporterName(reporter.getRealName() != null ? reporter.getRealName() : reporter.getUsername());
        }

        // 设置状态文本
        vo.setStatusText(getStatusText(report.getStatus()));

        return vo;
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 1 -> "待审核";
            case 2 -> "已通过";
            case 3 -> "已驳回";
            default -> "未知";
        };
    }
}
