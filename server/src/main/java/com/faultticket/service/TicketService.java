package com.faultticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.faultticket.common.BusinessException;
import com.faultticket.domain.dto.TicketDTO;
import com.faultticket.domain.pojo.Ticket;
import com.faultticket.domain.pojo.User;
import com.faultticket.domain.query.TicketPageQuery;
import com.faultticket.domain.vo.TicketVO;
import com.faultticket.mapper.TicketMapper;
import com.faultticket.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketMapper ticketMapper;
    private final UserMapper userMapper;

    public TicketVO create(TicketDTO dto, Long creatorId) {
        Ticket ticket = new Ticket();
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setCategory(dto.getCategory());
        ticket.setPriority(dto.getPriority() != null ? dto.getPriority() : 3);
        ticket.setStatus(1); // 待处理
        ticket.setCreatorId(creatorId);

        ticketMapper.insert(ticket);
        return convertToVO(ticket);
    }

    public Map<String, Object> page(TicketPageQuery query, Long currentUserId, Integer currentRole) {
        Page<Ticket> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();

        // 普通用户只能看自己创建的工单
        if (currentRole == 3) {
            wrapper.eq(Ticket::getCreatorId, currentUserId);
        }

        // 筛选条件
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(Ticket::getTitle, query.getKeyword());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Ticket::getStatus, query.getStatus());
        }
        if (query.getPriority() != null) {
            wrapper.eq(Ticket::getPriority, query.getPriority());
        }
        if (query.getCategory() != null) {
            wrapper.eq(Ticket::getCategory, query.getCategory());
        }

        wrapper.orderByDesc(Ticket::getCreateTime);

        Page<Ticket> result = ticketMapper.selectPage(page, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords().stream().map(this::convertToVO).toList());
        map.put("total", result.getTotal());
        map.put("page", result.getCurrent());
        map.put("pageSize", result.getSize());
        return map;
    }

    public TicketVO detail(Long id) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }
        return convertToVO(ticket);
    }

    public TicketVO update(Long id, TicketDTO dto, Long userId) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setCategory(dto.getCategory());
        if (dto.getPriority() != null) {
            ticket.setPriority(dto.getPriority());
        }

        ticketMapper.updateById(ticket);
        return convertToVO(ticket);
    }

    public void delete(Long id, Long userId, Integer role) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        // 只有管理员或创建人可以删除
        if (role != 1 && !ticket.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "无权限删除此工单");
        }

        ticketMapper.deleteById(id);
    }

    public TicketVO changeStatus(Long id, Integer newStatus, Long userId, Integer role) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        // 校验状态流转
        validateStatusChange(ticket.getStatus(), newStatus, ticket, userId, role);

        ticket.setStatus(newStatus);
        if (newStatus == 3) { // 已完成
            ticket.setResolveTime(LocalDateTime.now());
        }

        ticketMapper.updateById(ticket);
        return convertToVO(ticket);
    }

    public TicketVO assign(Long id, Long assigneeId) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        User assignee = userMapper.selectById(assigneeId);
        if (assignee == null) {
            throw new BusinessException(404, "指派的用户不存在");
        }

        ticket.setAssigneeId(assigneeId);
        if (ticket.getStatus() == 1) {
            ticket.setStatus(2); // 自动变为处理中
        }

        ticketMapper.updateById(ticket);
        return convertToVO(ticket);
    }

    /**
     * 用户验收工单
     */
    @Transactional
    public TicketVO acceptTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        // 只有创建人可以验收
        if (!ticket.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只有创建人可以验收工单");
        }

        // 工单必须是已完成状态
        if (ticket.getStatus() != 4) {
            throw new BusinessException(400, "只有已完成的工单可以验收");
        }

        ticket.setStatus(5); // 已结束
        ticket.setCloseTime(LocalDateTime.now());
        ticketMapper.updateById(ticket);

        return convertToVO(ticket);
    }

    /**
     * 用户提交满意度评价
     */
    @Transactional
    public TicketVO rateTicket(Long ticketId, Integer satisfaction, String comment, Long userId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        // 只有创建人可以评价
        if (!ticket.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只有创建人可以评价工单");
        }

        // 工单必须是已结束状态
        if (ticket.getStatus() != 5) {
            throw new BusinessException(400, "只有已结束的工单可以评价");
        }

        if (satisfaction < 1 || satisfaction > 5) {
            throw new BusinessException(400, "评分必须在1-5之间");
        }

        ticket.setSatisfaction(satisfaction);
        ticket.setSatisfactionComment(comment);
        ticketMapper.updateById(ticket);

        return convertToVO(ticket);
    }

    /**
     * 重新打开工单
     */
    @Transactional
    public TicketVO reopenTicket(Long ticketId, String reason, Long userId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        // 只有创建人可以重新打开
        if (!ticket.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只有创建人可以重新打开工单");
        }

        // 工单必须是已结束或已完成状态
        if (ticket.getStatus() != 4 && ticket.getStatus() != 5) {
            throw new BusinessException(400, "只有已完成或已结束的工单可以重新打开");
        }

        ticket.setStatus(2); // 回到处理中
        ticket.setCloseTime(null);
        ticket.setSatisfaction(null);
        ticket.setSatisfactionComment(null);
        ticketMapper.updateById(ticket);

        return convertToVO(ticket);
    }

    private void validateStatusChange(Integer current, Integer target, Ticket ticket, Long userId, Integer role) {
        // 已结束是终态（需要通过专门的接口操作）
        if (current == 5) {
            throw new BusinessException(400, "已结束的工单不能直接变更状态");
        }

        // 待处理 → 处理中（管理员指派或运维接单）
        if (current == 1 && target == 2) {
            if (role != 1 && role != 2) {
                throw new BusinessException(403, "无权操作");
            }
        }
        // 处理中 → 审核中（通过提交报告接口，不走这里）
        // 审核中 → 已完成（通过审核接口，不走这里）
        // 已完成 → 已结束（通过验收接口，不走这里）
        else {
            throw new BusinessException(400, "不允许的状态变更，请使用专用接口");
        }
    }

    private TicketVO convertToVO(Ticket ticket) {
        TicketVO vo = new TicketVO();
        vo.setId(ticket.getId());
        vo.setTitle(ticket.getTitle());
        vo.setDescription(ticket.getDescription());
        vo.setCategory(ticket.getCategory());
        vo.setCategoryName(getCategoryName(ticket.getCategory()));
        vo.setPriority(ticket.getPriority());
        vo.setPriorityName(getPriorityName(ticket.getPriority()));
        vo.setStatus(ticket.getStatus());
        vo.setStatusName(getStatusName(ticket.getStatus()));
        vo.setCreatorId(ticket.getCreatorId());
        vo.setAssigneeId(ticket.getAssigneeId());
        vo.setCreateTime(ticket.getCreateTime());
        vo.setUpdateTime(ticket.getUpdateTime());
        vo.setResolveTime(ticket.getResolveTime());
        vo.setCloseTime(ticket.getCloseTime());
        vo.setSatisfaction(ticket.getSatisfaction());
        vo.setSatisfactionComment(ticket.getSatisfactionComment());

        // 查询创建人和处理人姓名
        User creator = userMapper.selectById(ticket.getCreatorId());
        if (creator != null) {
            vo.setCreatorName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
        }
        if (ticket.getAssigneeId() != null) {
            User assignee = userMapper.selectById(ticket.getAssigneeId());
            if (assignee != null) {
                vo.setAssigneeName(assignee.getRealName() != null ? assignee.getRealName() : assignee.getUsername());
            }
        }

        return vo;
    }

    private String getCategoryName(Integer category) {
        return switch (category) {
            case 1 -> "网络故障";
            case 2 -> "设备故障";
            case 3 -> "服务异常";
            case 4 -> "其他";
            default -> "未知";
        };
    }

    private String getPriorityName(Integer priority) {
        return switch (priority) {
            case 1 -> "紧急";
            case 2 -> "高";
            case 3 -> "中";
            case 4 -> "低";
            default -> "未知";
        };
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 1 -> "待处理";
            case 2 -> "处理中";
            case 3 -> "审核中";
            case 4 -> "已完成";
            case 5 -> "已结束";
            default -> "未知";
        };
    }
}
