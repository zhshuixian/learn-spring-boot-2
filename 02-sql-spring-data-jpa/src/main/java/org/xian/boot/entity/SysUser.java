package org.xian.boot.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * @author xian
 */
@Entity
@Getter
@Setter
@Table(name = "sys_user", schema = "spring")
public class SysUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(length = 18, unique = true, nullable = false, name = "username")
    @NotEmpty(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{3,16}$", message = "用户名需3到16位的英文,数字")
    private String username;

    @Column(length = 18, nullable = false)
    @NotEmpty(message = "用户昵称不能为空")
    private String nickname;

    @Range(min=0, max=100,message = "年龄需要在 0 到 100 之间")
    private Integer userAge;

    @Column(length = 2)
    private String userSex;
}
