package org.xian.redis.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author xian
 */
@Entity
@Table(name = "user", schema = "spring")
public class User implements Serializable {
    private static final long serialVersionUID = 413797298970501130L;
    @Id
    private String username;
    private Byte age;


    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public Byte getAge() {
        return age;
    }

    public void setAge(Byte age) {
        this.age = age;
    }
}