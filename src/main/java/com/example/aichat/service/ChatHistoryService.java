package com.example.aichat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aichat.pojo.ChatHistory;

public interface ChatHistoryService {
    void saveRecord(ChatHistory record);
    Page<ChatHistory> getHistoryByUserId(Integer userId, Integer pageNum, Integer pageSize);
}