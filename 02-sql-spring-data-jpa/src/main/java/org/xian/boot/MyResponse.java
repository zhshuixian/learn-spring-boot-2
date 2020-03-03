package org.xian.boot;

import java.io.Serializable;
import lombok.*;

/**
 * 将操作结果或者错误信息返回
 * @author xian
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MyResponse implements Serializable {
    private static final long serialVersionUID = -2L;
    private String status;
    private String message;
}
