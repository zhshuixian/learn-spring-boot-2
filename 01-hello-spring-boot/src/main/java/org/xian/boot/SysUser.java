package org.xian.boot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * @author xian
 */
@AllArgsConstructor
@Setter
@Getter
public class SysUser {
    private String username;
    private String password;
}
