package com.faultticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.faultticket.common.BusinessException;
import com.faultticket.domain.dto.CommentDTO;
import com.faultticket.domain.pojo.Comment;
import com.faultticket.domain.pojo.Ticket;
import com.faultticket.domain.pojo.User;
import com.faultticket.domain.vo.CommentVO;
import com.faultticket.mapper.CommentMapper;
import com.faultticket.mapper.TicketMapper;
import com.faultticket.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final TicketMapper ticketMapper;
    private final UserMapper userMapper;

    public CommentVO add(Long ticketId, CommentDTO dto, Long userId) {
        // 校验工单是否存在
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        Comment comment = new Comment();
        comment.setTicketId(ticketId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent());

        commentMapper.insert(comment);
        return convertToVO(comment);
    }

    public List<CommentVO> listByTicketId(Long ticketId) {
        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getTicketId, ticketId)
                        .orderByAsc(Comment::getCreateTime)
        );

        return comments.stream().map(this::convertToVO).toList();
    }

    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setTicketId(comment.getTicketId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());

        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
        }

        return vo;
    }
}
