package com.faultticket.controller;

import com.faultticket.common.Result;
import com.faultticket.domain.dto.CommentDTO;
import com.faultticket.domain.vo.CommentVO;
import com.faultticket.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public Result<CommentVO> add(@PathVariable Long ticketId, @Valid @RequestBody CommentDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("评论成功", commentService.add(ticketId, dto, userId));
    }

    @GetMapping
    public Result<List<CommentVO>> list(@PathVariable Long ticketId) {
        return Result.success(commentService.listByTicketId(ticketId));
    }
}
