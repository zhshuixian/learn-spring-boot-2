package org.xian.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author xiaoxian
 */
@RestController
public class ErrorController {

    @RequestMapping("/throw")
    public String myThrow() throws MyException {
        // Throw MyException
        throw new MyException("Error", "Throw MyException");
    }

    @PostMapping("/user")
    public String user(@RequestBody @Valid final User user) {
        // 参数检验
        return "参数检验通过";
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public MyResponse httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return new MyResponse("ERROR", e.getMessage());
    }
}
