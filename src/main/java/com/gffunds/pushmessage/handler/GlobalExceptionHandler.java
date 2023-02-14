package com.gffunds.pushmessage.handler;


import com.gffunds.pushmessage.common.ReturnResult;
import com.gffunds.pushmessage.common.enumeration.ErrCodeEnum;
import com.gffunds.pushmessage.exception.PushMessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 全局异常处理
 *
 * @author s-liux
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 全局异常处理示例 实际使用时，什么错误返回什么消息和状态码根据实际项目需求来，自行修改 {@code @ExceptionHandler}指定的异常类可以改为想要捕获的更具体的异常子类
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(PushMessageException.class)
    public ReturnResult exceptionHandler(Exception e) {
        //打印异常堆栈到日志
        log.error(e.getMessage(), e);
        return new ReturnResult<>(ErrCodeEnum.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public ReturnResult handleRestException(Exception e) {
        log.error("业务执行异常", e);
        return new ReturnResult<>(ErrCodeEnum.REST_EXCEPTION);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public Object handleParamErrorException(Exception e) {
        log.error(e.getMessage(), e);
        return new ReturnResult<>(ErrCodeEnum.REQUEST_ABNORMAL);
    }

    /**
     * 方法参数@RequestParam、@RequestPart、@PathVariable校验异常处理，
     * 本质是spring的bean的method validation
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(ConstraintViolationException.class)
    public ReturnResult handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {

        logRequest(e, request);
        String errorMessage = Optional.ofNullable(e.getConstraintViolations()).orElseGet(Collections::emptySet).stream()
                .map(ConstraintViolation::getMessage)
                .map(msg -> "参数错误：" + msg)
                .collect(Collectors.joining(System.lineSeparator()));
        return new ReturnResult(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                "错误信息：" + e.getMessage() + System.lineSeparator() + "校验信息：" + errorMessage
        );
    }

    /**
     * 处理 @RequestBody 注解的参数的属性校验时抛出的异常
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ReturnResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {

        logRequest(e, request);
        String message = Optional.ofNullable(e.getBindingResult()).map(Errors::getAllErrors).orElseGet(Collections::emptyList)
                .stream()
                .map(oe -> String.format("参数[%s]错误：%s；请求值：%s", oe.getObjectName(), oe.getDefaultMessage(), Arrays.toString(oe.getArguments())))
                .collect(Collectors.joining(System.lineSeparator()));
        return new ReturnResult(String.valueOf(HttpStatus.BAD_REQUEST.value()), message);
    }

    /**
     * 处理Get请求中 使用@Valid 验证路径中请求实体校验失败后抛出的异常，详情继续往下看代码
     *
     * @author s-zengc
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public ReturnResult handleBindException(BindException e, HttpServletRequest request) {
        logRequest(e, request);
        String message = Optional.ofNullable(e.getBindingResult()).map(Errors::getAllErrors).orElseGet(Collections::emptyList)
                .stream()
                .map(oe -> String.format("参数[%s]错误：%s；请求值：%s", oe.getObjectName(), oe.getDefaultMessage(), Arrays.toString(oe.getArguments())))
                .collect(Collectors.joining(System.lineSeparator()));
        return new ReturnResult(String.valueOf(HttpStatus.BAD_REQUEST.value()), message);
    }

    private String logRequest(Exception e, HttpServletRequest request) {
        String logMessage = String.format("请求[%s]:[%s]", request.getRequestURI(), e.getMessage());
        log.error(logMessage, e);
        return e.getMessage();
    }
}
