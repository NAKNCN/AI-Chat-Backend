// 1. 包声明必须和你的文件夹结构完全一致（service_impl）
package com.example.aichat.service.impl;

// 2. 导入所有需要的类，解决"无法解析符号"问题
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aichat.mapper.UserMapper;
import com.example.aichat.pojo.User;
import com.example.aichat.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;

// 3. 类名必须和文件名 UserServiceImpl.java 完全一致
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 构造器注入（Spring推荐方式，无警告）
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // 实现UserService接口的所有方法
    @Override
    public String isAdult(Integer age) {
        return age >= 18 ? "已成年" : "未成年";
    }

    @Override
    public String getUserInfo(String name, Integer age) {
        String status = isAdult(age);
        return "姓名: " + name + ", 年龄: " + age + ", 状态: " + status;
    }

    @Override
    public User getUserById(Integer id) {
        // 调用MyBatis-Plus内置方法查询数据库
        return userMapper.selectById(id);
    }

    @Override
    public Boolean addUser(User user) {
        return userMapper.insert(user) > 0;
    }

    @Override
    public Boolean updateUser(User user){
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        int rows = userMapper.updateById(user);
        return rows > 0;
    }

    @Override
    public Boolean deleteUser(Integer id) {
        int row = userMapper.deleteById(id);
        return row > 0;
    }

    @Override
    public List<User> searchUser(@RequestParam(required = false)String username){
        QueryWrapper<User>wrapper = new  QueryWrapper<>();
        if(username != null && !username.isEmpty()){
            wrapper.like("username",username);
        }
        return  userMapper.selectList(wrapper);
    }

    @Override
    public Page<User> pageUser(Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        return userMapper.selectPage(page, null);
    }

    @Override
    public User getByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).one();
    }
}