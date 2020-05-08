package org.xian.lock;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaoxian
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RedisLock {

    /**
     * 锁的 key 值，必须为非空，非空字符串
     *
     * @return 锁的 key 值
     */
    @NotNull
    @NotEmpty
    String key();

    /**
     * 锁的 value 值
     *
     * @return value 值
     */
    String value() default "";


    /**
     * 默认锁有效期 默认 15 秒
     *
     * @return 锁有效期
     */
    long expire() default 15;

    /**
     * 锁有效期期的时间单位，默认 秒
     *
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
