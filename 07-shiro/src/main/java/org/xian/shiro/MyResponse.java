package org.xian.shiro;

import java.io.Serializable;

public class MyResponse implements Serializable {
    private static final long serialVersionUID = -2L;
    private String status;
    private String message;

    public MyResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public MyResponse() {
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}