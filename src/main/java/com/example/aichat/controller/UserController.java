package com.example.aichat.controller;

import org.springframework.web.bind.annotation.*;
import com.example.aichat.pojo.Result;
import com.example.aichat.pojo.User;
import com.example.aichat.service.UserService;
import java.util.List;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//Controller负责请求转发，接受页面过来的参数，传给Service处理，接到返回值，再传给页面。
//
@CrossOrigin
@RestController
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/query/{id}")
    public Result queryUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.fail(404, "该用户不存在");
        }
        return Result.success(user);
    }

    @PostMapping("user/add")
    public Result addUser(@RequestBody User user) {
        // 调用Service保存用户
        Boolean success = userService.addUser(user);
        // 返回成功信息
        if(success){
            return Result.success("新增成功");
        } else {
            return Result.fail(500,"新增失败");
        }
    }

    @PutMapping("/user/update")
    public Result updateUser(@RequestBody User user) {
        // 调用Service保存用户
        Boolean success = userService.updateUser(user);
        // 返回成功信息
        if(success){
            return Result.success("修改成功");
        } else {
            return Result.fail(500,"用户不存在");
        }
    }

    @DeleteMapping("/user/delete/{id}")
    public Result deleteUserById(@PathVariable Integer id) {
        Boolean success = userService.deleteUser(id);
        if(success){
            return Result.success("删除成功");
        }
        return Result.fail(404,"删除失败");
    }

    @GetMapping("/user/search")
    public Result searchUser(@RequestParam(required = false) String username){
        List<User> list = userService.searchUser(username);
        return Result.success(list);
    }

    @GetMapping("/user/page")
    public Result pageUser(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<User> page = userService.pageUser(pageNum, pageSize);
        return Result.success(page);
    }

//    @GetMapping("/test/error")
//    public Result testError() {
//        int i = 1 / 0;
//        return Result.success("不会执行到这里");
//    }
}