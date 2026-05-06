package com.example.aichat.pojo;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户实体类，对应数据库的user表
 */
@Data
@TableName("user") // 核心注解：指定这个类对应数据库里的user表
public class User {
    // 对应表的主键id
    private Integer id;
    // 用户名
    private String username;
    // 密码
    private String password;
    // 年龄
    private Integer age;
}