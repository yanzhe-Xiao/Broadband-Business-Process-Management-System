package com.xyz.advice;

import com.xyz.common.ResponseResult;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseResult<Void> handleBadCred(BadCredentialsException e) {
        return ResponseResult.fail(401, "用户名或密码错误");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseResult<Void> handleParam(Exception e) {
        return ResponseResult.fail(400, "请求参数不合法");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseResult<Void> handleBiz(RuntimeException e) {
        return ResponseResult.fail(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseResult<Void> handleOther(Exception e) {
        return ResponseResult.fail(500, "服务器开小差了");
    }
}