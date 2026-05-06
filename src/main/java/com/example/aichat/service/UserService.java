package com.example.aichat.service;

import com.example.aichat.pojo.User;
import java.util.List;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface UserService {
    // 你之前的方法保留
    String isAdult(Integer age);
    String getUserInfo(String name, Integer age);
    // 新增：根据ID查询用户
    User getUserById(Integer id);
    Boolean addUser(User user);
    Boolean updateUser(User user);
    Boolean deleteUser(Integer id);
    List<User> searchUser(String username);
    Page<User> pageUser(Integer pageNum, Integer pageSize);
}
