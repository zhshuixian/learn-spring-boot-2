package org.xian.boot.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author xian
 */
@Entity
@Getter
@Setter
@Table(name = "sys_user")
public class SysUser {
    @Id
    private Long  userId;

    @Column(length = 16)
    private String  username;

    private Integer userAge;

    @Column(length = 128)
    private String  userAddress;
}
