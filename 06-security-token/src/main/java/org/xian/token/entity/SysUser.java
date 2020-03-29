package org.xian.token.entity;

import java.io.Serializable;

/**
 * @author xian
 */
public class SysUser implements Serializable {
    private static final long serialVersionUID = 4522943071576672084L;

    private Long userId;

    private String username;

    private String password;

    private String userRole;


    public SysUser() {

    }

    public SysUser(Long userId, String username, String password, String userRole) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.userRole = userRole;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}