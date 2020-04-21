package org.xian.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * 拦截系统错误信息，封装成统一样式返回前端
 *
 * @author xian
 */

@RestControllerAdvice
public class ErrorRestControllerAdvice {
    /**
     * 全局异常捕捉处理 返回 401 状态
     *
     * @param ex Exception
     * @return 包含的错误信息
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MyResponse errorHandler(Exception ex) {
        return new MyResponse("ERROR", ex.getMessage());
    }

    /**
     * 自定义异常捕获,返回 500 状态
     *
     * @param e Exception
     * @return 错误信息和状态
     */
    @ExceptionHandler(value = MyException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MyResponse myException(MyException e) {
        return new MyResponse(e.getStatus(), e.getMessage());
    }

    /**
     * Http Method 异常 返回 405
     *
     * @param e Exception
     * @return 错误信息
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public MyResponse httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return new MyResponse("ERROR", e.getMessage());
    }

    /**
     * 404异常,返回 404 异常
     * @param e NoHandlerFoundException
     * @return 资源不存在
     */
    @ExceptionHandler(value = NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public MyResponse noHandlerFoundException(NoHandlerFoundException e) {
        return new MyResponse("ERROR", "资源不存在");
    }

    /**
     * RequestBody 为空时返回此错误提醒,返回400 bad Request
     *
     * @return 请传入参数
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MyResponse httpMessageNotReadableException() {
        return new MyResponse("ERROR", "请传入参数");
    }

    /**
     * RequestBody某个必须输入的参数为空时 返回 400 Bad Request
     *
     * @param ex 错误信息
     * @return 错误信息
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MyResponse methodDtoNotValidException(Exception ex) {
        MethodArgumentNotValidException c = (MethodArgumentNotValidException) ex;
        List<ObjectError> errors = c.getBindingResult().getAllErrors();
        StringBuffer errorMsg = new StringBuffer();
        errors.forEach(x -> errorMsg.append(x.getDefaultMessage()).append(" "));
        return new MyResponse("ERROR", errorMsg.toString());
    }

}
