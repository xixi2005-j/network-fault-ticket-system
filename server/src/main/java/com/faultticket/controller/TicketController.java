package com.faultticket.controller;

import com.faultticket.common.Result;
import com.faultticket.domain.dto.TicketDTO;
import com.faultticket.domain.query.TicketPageQuery;
import com.faultticket.domain.vo.TicketVO;
import com.faultticket.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public Result<TicketVO> create(@Valid @RequestBody TicketDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("创建成功", ticketService.create(dto, userId));
    }

    @GetMapping
    public Result<Map<String, Object>> page(TicketPageQuery query, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        return Result.success(ticketService.page(query, userId, role));
    }

    @GetMapping("/{id}")
    public Result<TicketVO> detail(@PathVariable Long id) {
        return Result.success(ticketService.detail(id));
    }

    @PutMapping("/{id}")
    public Result<TicketVO> update(@PathVariable Long id, @Valid @RequestBody TicketDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("更新成功", ticketService.update(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        ticketService.delete(id, userId, role);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<TicketVO> changeStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        return Result.success("状态变更成功", ticketService.changeStatus(id, status, userId, role));
    }

    @PutMapping("/{id}/assign")
    public Result<TicketVO> assign(@PathVariable Long id, @RequestParam Long assigneeId) {
        return Result.success("指派成功", ticketService.assign(id, assigneeId));
    }
}
