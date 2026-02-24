package com.southcollege.exam.exception;

import com.southcollege.exam.dto.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TEXT_EVENT_STREAM = "text/event-stream";

    /**
     * 检查是否为 SSE 请求
     * 检查 Accept 头和响应的 Content-Type
     */
    private boolean isSseRequest(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains(TEXT_EVENT_STREAM);
    }

    /**
     * 检查异常是否与 SSE 相关
     * 通过检查异常消息或堆栈来判断是否来自 SSE 上下文
     */
    private boolean isSseRelatedException(Exception e) {
        String message = e.getMessage();
        if (message != null) {
            return message.contains("text/event-stream") ||
                   message.contains("SSE") ||
                   message.contains("AI调用失败");
        }
        return false;
    }

    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {}", e.getMessage());
        if (isSseRequest(request)) {
            // SSE 请求不返回 JSON 响应，避免 Content-Type 冲突
            return null;
        }
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        if (isSseRequest(request)) {
            return null;
        }
        return Result.error(400, message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        if (isSseRequest(request)) {
            return null;
        }
        return Result.error(400, message);
    }

    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: {}", e.getMessage(), e);
        if (isSseRequest(request) || isSseRelatedException(e)) {
            // SSE 请求或 SSE 相关异常不返回 JSON 响应，避免 Content-Type 冲突
            return null;
        }
        return Result.error("系统错误，请联系管理员");
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {}", e.getMessage(), e);
        if (isSseRequest(request) || isSseRelatedException(e)) {
            // SSE 请求或 SSE 相关异常不返回 JSON 响应，避免 Content-Type 冲突
            return null;
        }
        return Result.error(500, "系统错误，请联系管理员");
    }
}
