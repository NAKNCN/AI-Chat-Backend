package com.example.aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aichat.mapper.ChatHistoryMapper;
import com.example.aichat.pojo.ChatHistory;
import com.example.aichat.service.ChatHistoryService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private final ChatHistoryMapper chatHistoryMapper;

    public ChatHistoryServiceImpl(ChatHistoryMapper chatHistoryMapper) {
        this.chatHistoryMapper = chatHistoryMapper;
    }

    @Async
    @Override
    public void saveRecord(ChatHistory record) {
        chatHistoryMapper.insert(record);
    }

    @Override
    public Page<ChatHistory> getHistoryByUserId(Integer userId, Integer pageNum, Integer pageSize) {
        Page<ChatHistory> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ChatHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        return chatHistoryMapper.selectPage(page, wrapper);
    }
}