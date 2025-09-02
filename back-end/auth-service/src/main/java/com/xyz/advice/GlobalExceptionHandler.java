package com.xyz.advice;

import com.xyz.common.ResponseResult;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/**
 * <p>Package Name: com.xyz.advice </p>
 * <p>Description: 全局异常处理程序，用于捕获和处理控制器层抛出的各种异常，并返回统一格式的响应结果。</p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理认证失败异常（例如：用户名或密码错误）。
     * 当Spring Security认证失败并抛出BadCredentialsException时，此方法将被调用。
     *
     * @param e 捕获到的 BadCredentialsException 异常对象。
     * @return 返回一个包含401状态码和错误信息的通用响应结果。
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseResult<Void> handleBadCred(BadCredentialsException e) {
        return ResponseResult.fail(401, "用户名或密码错误");
    }

    /**
     * 处理参数验证相关的异常。
     * 这包括：
     * - {@link MethodArgumentNotValidException}: 当使用@Valid注解的请求体（Request Body）验证失败时抛出。
     * - {@link ConstraintViolationException}: 当使用@Validated注解的方法参数或路径变量验证失败时抛出。
     * - {@link HttpMessageNotReadableException}: 当请求体无法被正确解析时（例如JSON格式错误）抛出。
     *
     * @param e 捕获到的异常对象。
     * @return 返回一个包含400状态码和“请求参数不合法”信息的通用响应结果。
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseResult<Void> handleParam(Exception e) {
        return ResponseResult.fail(400, "请求参数不合法");
    }

    /**
     * 处理业务逻辑层抛出的运行时异常。
     * 这是为了捕获在服务层（Service）等业务代码中主动抛出的、未被特定处理的运行时异常。
     *
     * @param e 捕获到的 RuntimeException 异常对象。
     * @return 返回一个包含400状态码和具体异常信息的通用响应结果。
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseResult<Void> handleBiz(RuntimeException e) {
        return ResponseResult.fail(400, e.getMessage());
    }

    /**
     * 处理所有其他未被捕获的异常。
     * 这是一个兜底的异常处理器，用于捕获代码中任何未被前面@ExceptionHandler处理的异常，防止将详细的、可能包含敏感信息的堆栈跟踪信息暴露给客户端。
     *
     * @param e 捕获到的 Exception 异常对象。
     * @return 返回一个包含500状态码和通用错误信息的通用响应结果，提示服务器内部错误。
     */
    @ExceptionHandler(Exception.class)
    public ResponseResult<Void> handleOther(Exception e) {
        return ResponseResult.fail(500, "服务器开小差了");
    }
}