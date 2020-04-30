package org.xian.boot.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author xian
 */
@Data
public class SysUser implements Serializable {
    private static final long serialVersionUID = 4522943071576672084L;

    private Long userId;

    @NotEmpty(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{3,16}$", message = "用户名需3到16位的英文,数字")
    private String username;

    @NotEmpty(message = "用户昵称不能为空")
    private String nickname;

    @Range(min=0, max=100,message = "年龄需要在 0 到 100 之间")
    private Integer userAge;

    private String userSex;
}
