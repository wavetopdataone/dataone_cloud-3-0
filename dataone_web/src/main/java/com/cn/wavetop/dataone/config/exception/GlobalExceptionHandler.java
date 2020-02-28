package com.cn.wavetop.dataone.config.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;


@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义的业务异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = BizException.class)
    @ResponseBody
    public ResultBody bizExceptionHandler(HttpServletRequest req, BizException e){
        logger.error("*发生业务异常！原因是：{}"+e);
        return ResultBody.error(e.getErrorCode(),e.getErrorMsg());
    }


    /**
     * 处理空指针的异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =NullPointerException.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, NullPointerException e){
        logger.error("*发生空指针异常！原因是:",e);


        return ResultBody.error(CommonEnum.NULL_NOT_MATCH);
    }
    /**
     * 接口参数转化异常 本项目都是String传的是空的 转化long报错
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =NumberFormatException.class)
    @ResponseBody
    public ResultBody NumberFormatException(HttpServletRequest req, NumberFormatException e){
        logger.error("*数字格式化异常:",e);
        return ResultBody.error(CommonEnum.NUMBER_FROMAT_ERROR);
    }
    /**
     * 接口参数转化异常 本项目都是String传的是空的 转化long报错
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ResultBody MethodArgumentTypeMismatchException(HttpServletRequest req, MethodArgumentTypeMismatchException e){
        logger.error("*接口参数转化异常:",e);
        return ResultBody.error(CommonEnum.NUMBER_FROMAT_ERROR);
    }

    /**
     * 入参和形参不一致，导致参数接受不到
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = InvalidDataAccessApiUsageException.class)
    @ResponseBody
    public ResultBody InvalidDataAccessApiUsageException(HttpServletRequest req, InvalidDataAccessApiUsageException e){
        logger.error("*参数不匹配:",e);


        return ResultBody.error(CommonEnum.PARAMS_NOT_ERROR);
    }

    /**
     * 处理请求方法不支持的异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, HttpRequestMethodNotSupportedException e){
        logger.error("*发生请求方法不支持异常！原因是:",e);
        return ResultBody.error(CommonEnum.REQUEST_METHOD_SUPPORT_ERROR);
    }
    //类型转换异常
    @ExceptionHandler(ClassCastException.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, ClassCastException ex) {
        logger.error("*类型转换异常",ex);
        return ResultBody.error(CommonEnum.TYPE_NOT_ERROR);
    }

    //IO异常
    @ExceptionHandler(IOException.class)
    @ResponseBody
    public ResultBody iOExceptionHandler(IOException ex) {
        logger.error("*IO异常",ex);
        return ResultBody.error(CommonEnum.INTERNAL_SERVER_ERROR);
    }
    //数据下标越界
    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    @ResponseBody
    public ResultBody ArrayIndexOutOfBoundsExceptionHandler(ArrayIndexOutOfBoundsException ex) {
        logger.error("*数据下标越界",ex);
        return ResultBody.error(CommonEnum.INTERNAL_SERVER_ERROR);
    }
    //系统找不到指定文件
    @ExceptionHandler(FileNotFoundException.class)
    @ResponseBody
    public ResultBody FileNotFoundException(FileNotFoundException ex) {
        logger.error("*系统找不到指定文件",ex);
        return ResultBody.error(CommonEnum.NOT_FOUND_FILE);
    }

    /**
     * 处理其他异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =Exception.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, Exception e){
        logger.error("*未知异常！原因是:",e);
        return ResultBody.error(CommonEnum.SERVER_BUSY);
    }
}
