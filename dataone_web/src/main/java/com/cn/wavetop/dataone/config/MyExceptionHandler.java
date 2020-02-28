//package com.cn.wavetop.dataone.config;
//
//import com.alibaba.fastjson.JSON;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.ConversionNotSupportedException;
//import org.springframework.beans.TypeMismatchException;
//import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.http.converter.HttpMessageNotWritableException;
//import org.springframework.web.HttpMediaTypeNotAcceptableException;
//import org.springframework.web.HttpRequestMethodNotSupportedException;
//import org.springframework.web.bind.MissingServletRequestParameterException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//import java.io.IOException;
//
///**
// * 描述：全局异常处理类
// *  异常处理拦截器
// * User: 薛子豪
// * Date: 2019-12-6
// * Time: 20:15
// */
//@CrossOrigin
//@ControllerAdvice
//@ResponseBody
//@Slf4j
//public class MyExceptionHandler {
//    private static Logger logger = LoggerFactory.getLogger(MyExceptionHandler.class);
//
//    //运行时异常
//    @ExceptionHandler(RuntimeException.class)
//    public String runtimeExceptionHandler(RuntimeException ex) {
//        log.error("运行时异常",ex);
//        return exceptionFormat(ex);
//    }
//
//    //空指针异常
//    @ExceptionHandler(NullPointerException.class)
//    public String nullPointerExceptionHandler(NullPointerException ex) {
//        log.error("空指针异常",ex);
//        return exceptionFormat(ex);
//    }
//
//    //类型转换异常
//    @ExceptionHandler(ClassCastException.class)
//    public String classCastExceptionHandler(ClassCastException ex) {
//        log.error("类型转换异常",ex);
//        return exceptionFormat(ex);
//    }
//
//    //IO异常
//    @ExceptionHandler(IOException.class)
//    public String iOExceptionHandler(IOException ex) {
//        log.error("IO异常",ex);
//        return exceptionFormat(ex);
//    }
//
//    //未知方法异常
//    @ExceptionHandler(NoSuchMethodException.class)
//    public String noSuchMethodExceptionHandler(NoSuchMethodException ex) {
//        log.error("未知方法异常",ex);
//        return exceptionFormat(ex);
//    }
//
//    //数组越界异常
//    @ExceptionHandler(IndexOutOfBoundsException.class)
//    public String indexOutOfBoundsExceptionHandler(IndexOutOfBoundsException ex) {
//        log.error("*数组越界异常",ex);
//        return exceptionFormat(ex);
//    }
//
//    //400错误
//    @ExceptionHandler({HttpMessageNotReadableException.class})
//    public String requestNotReadable(HttpMessageNotReadableException ex) {
//        log.error("400..requestNotReadable");
//        return exceptionFormat(ex);
//    }
//
//    //400错误
//    @ExceptionHandler({TypeMismatchException.class})
//    public String requestTypeMismatch(TypeMismatchException ex) {
//        log.error("400..TypeMismatchException");
//        return exceptionFormat(ex);
//    }
//
//    //400错误
//    @ExceptionHandler({MissingServletRequestParameterException.class})
//    public String requestMissingServletRequest(MissingServletRequestParameterException ex) {
//        log.error("400..MissingServletRequest");
//        return exceptionFormat(ex);
//    }
//
//    //405错误
//    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
//    public String request405(HttpRequestMethodNotSupportedException ex) {
//        log.error("405...异常",ex);
//        return exceptionFormat(ex);
//    }
//
//    //406错误
//    @ExceptionHandler({HttpMediaTypeNotAcceptableException.class})
//    public String request406(HttpMediaTypeNotAcceptableException ex) {
//        log.error("406...",ex);
//        return exceptionFormat(ex);
//    }
//
//    //500错误
//    @ExceptionHandler({ConversionNotSupportedException.class, HttpMessageNotWritableException.class})
//    public String server500(RuntimeException ex) {
//        log.error("500...",ex);
//        return exceptionFormat(ex);
//    }
//
//    //栈溢出
//    @ExceptionHandler({StackOverflowError.class})
//    public String requestStackOverflow(StackOverflowError ex) {
//        log.error("栈溢出异常",ex);
//        return exceptionFormat(ex);
//    }
//
//    //其他错误
//    @ExceptionHandler({Exception.class})
//    public String exception(Exception ex) {
//        log.error("其他异常",ex);
//        return exceptionFormat(ex);
//    }
//
//    private <T extends Throwable> String exceptionFormat(T ex) {
//        return JSON.toJSONString(ResponseModel.error(ex.getMessage()));
//    }
//}
