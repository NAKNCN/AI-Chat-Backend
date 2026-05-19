package com.example.aichat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aichat.pojo.ChatHistory;
import java.util.List;

public interface ChatHistoryService {
    void saveRecord(ChatHistory record);
    Page<ChatHistory> getHistoryByUserId(Integer userId, Integer pageNum, Integer pageSize);

    // 新增：查询指定用户最近 N 条对话记录
    List<ChatHistory> getRecentHistory(Integer userId, int limit);
    void save(ChatHistory chatHistory);
}