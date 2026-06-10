package com.faultticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.faultticket.domain.pojo.Ticket;
import com.faultticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TicketMapper ticketMapper;

    public Map<String, Object> overview() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", ticketMapper.selectCount(null));
        result.put("pending", ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>().eq(Ticket::getStatus, 1)));
        result.put("processing", ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>().eq(Ticket::getStatus, 2)));
        result.put("reviewing", ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>().eq(Ticket::getStatus, 3)));
        result.put("completed", ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>().eq(Ticket::getStatus, 4)));
        result.put("closed", ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>().eq(Ticket::getStatus, 5)));
        return result;
    }

    public List<Map<String, Object>> byCategory() {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] names = {"网络故障", "设备故障", "服务异常", "其他"};
        for (int i = 1; i <= 4; i++) {
            Long count = ticketMapper.selectCount(
                    new LambdaQueryWrapper<Ticket>().eq(Ticket::getCategory, i));
            Map<String, Object> item = new HashMap<>();
            item.put("name", names[i - 1]);
            item.put("value", count);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> byPriority() {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] names = {"紧急", "高", "中", "低"};
        for (int i = 1; i <= 4; i++) {
            Long count = ticketMapper.selectCount(
                    new LambdaQueryWrapper<Ticket>().eq(Ticket::getPriority, i));
            Map<String, Object> item = new HashMap<>();
            item.put("name", names[i - 1]);
            item.put("value", count);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> trend(Integer days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            Long count = ticketMapper.selectCount(
                    new LambdaQueryWrapper<Ticket>()
                            .between(Ticket::getCreateTime, start, end));

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            result.add(item);
        }
        return result;
    }
}
