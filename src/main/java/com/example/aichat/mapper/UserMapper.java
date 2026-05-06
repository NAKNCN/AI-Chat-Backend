package com.example.aichat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aichat.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口，负责和数据库打交道
 * 继承BaseMapper<User>，直接获得所有增删改查方法
 */
@Mapper // 标记这是Mapper接口
public interface UserMapper extends BaseMapper<User> {
    // 不用写任何代码，BaseMapper已经内置了所有常用方法
}