package com.example.aichat.exception;

import com.example.aichat.pojo.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获所有未处理的异常
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
// 实际生产环境这里应记录日志，此处先打印便于调试
        e.printStackTrace();
        return Result.fail(500, "服务器内部错误，请稍后重试");
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFoundException(NoResourceFoundException e) throws NoResourceFoundException {
        // 直接抛出，让Spring Boot默认处理，以便在控制台查看原始错误
        throw e;
    }
// 可根据需要添加更具体的异常处理方法，例如：
// @ExceptionHandler(NullPointerException.class)
// public Result handleNullPointer(NullPointerException e) {
// return Result.fail(500, "空指针异常");
// }
}