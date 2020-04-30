package org.xian.error;

/**
 * @author xiaoxian
 */
public class MyException  extends Throwable{
    private final String status;
    private final String  message;

    public MyException(String  status,String message) {
        this.status=status;
       this.message=message;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
