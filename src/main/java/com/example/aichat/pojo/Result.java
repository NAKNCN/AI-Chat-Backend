package com.example.aichat.pojo;

import lombok.Getter;
import lombok.Setter;

// 统一返回结果类（前后端交互的标准格式）
@Setter
@Getter
public class Result {
    private static int i;
    // Getter/Setter（IDE会自动生成，复制代码后按Alt+Insert即可）
    private Integer code; // 响应码：200成功，400/404/500失败
    private String msg;   // 提示信息
    private Object data;  // 返回的数据（可以是User、List等）

    // 私有构造，只能用静态方法创建对象
    private Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功响应（带数据）
    public static Result success(Object data) {
        return new Result(200, "操作成功", data);
    }

    // 失败响应（自定义错误码和信息）
    public static Result fail(Integer code, String msg) {
        return new Result(code, msg, null);
    }

    public static Result error(int i, @SuppressWarnings("NonAsciiCharacters") String 文档内容不能为空) {
        return null;
    }

}