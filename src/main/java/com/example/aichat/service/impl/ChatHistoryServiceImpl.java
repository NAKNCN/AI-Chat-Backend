package com.example.aichat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aichat.mapper.ChatHistoryMapper;
import com.example.aichat.pojo.ChatHistory;
import com.example.aichat.service.ChatHistoryService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private final ChatHistoryMapper chatHistoryMapper;

    public ChatHistoryServiceImpl(ChatHistoryMapper chatHistoryMapper) {
        this.chatHistoryMapper = chatHistoryMapper;
    }

    @Override
    public void saveRecord(ChatHistory record) {
        chatHistoryMapper.insert(record);
    }

    @Override
    public void save(ChatHistory chatHistory) {
        chatHistoryMapper.insert(chatHistory);
    }

    @Override
    public Page<ChatHistory> getHistoryByUserId(Integer userId, Integer pageNum, Integer pageSize) {
        Page<ChatHistory> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ChatHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        return chatHistoryMapper.selectPage(page, wrapper);
    }

    @Override
    public List<ChatHistory> getRecentHistory(Integer userId, int limit) {
        QueryWrapper<ChatHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("create_time")
                .last("LIMIT " + limit);
        List<ChatHistory> list = chatHistoryMapper.selectList(wrapper);
        java.util.Collections.reverse(list);
        return list;
    }
}