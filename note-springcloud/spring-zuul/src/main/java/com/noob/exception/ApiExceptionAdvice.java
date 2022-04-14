package com.noob.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice(annotations = RestController.class)
public class ApiExceptionAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionAdvice.class);


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String handleBindExceptionException(Exception ex) {
        return "RuntimeException_fire";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String handleMethodArgumentException(MethodArgumentNotValidException ex) {
        return "MethodArgumentNotValidException";
    }


    /**
     * 处理参数有误,json不能正确转换为对象异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String handleHttpMessageConversionException(HttpMessageConversionException ex) {
        LOGGER.error(ex.getMessage(), ex);
        String msg;
        Throwable throwable = ex.getCause();
        if (throwable instanceof InvalidFormatException) {
            InvalidFormatException ex2 = (InvalidFormatException) throwable;
            msg = String.format("参数类型不匹配{%s},required：{%s}", ex2.getValue(), ex2.getTargetType().getName());
        } else if (throwable instanceof JsonParseException) {
            JsonParseException ex2 = (JsonParseException) throwable;
            msg = String.format("参数有误,json数据格式有误 %s", ex2.getOriginalMessage());
        } else if (throwable instanceof JsonMappingException) {
            msg = String.format("参数有误,参数格式错误,转换失败 ");
        } else {
            msg = String.format("参数有误  %s", ex.getMessage());
        }
        return msg;
    }


}
