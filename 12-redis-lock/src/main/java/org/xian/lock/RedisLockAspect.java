package org.xian.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.UUID;


/**
 * @author xiaoxian
 */

@Component
@Aspect
public class RedisLockAspect {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private RedisLockUtil redisLockUtil;
    private String lockKey;
    private String lockValue;

    /**
     * 切入点 RedisLock.java
     */
    @Pointcut("@annotation(org.xian.lock.RedisLock)")
    public void pointcut() {
    }

    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint joinPoint)  {
        // 获得其 @RedisLock 注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedisLock redisLock = method.getAnnotation(RedisLock.class);
        // 获取注解上 key value 等值
        lockKey = redisLock.key();
        lockValue = redisLock.value();
        if (lockValue.isEmpty()) {
            lockValue = UUID.randomUUID().toString();
        }
        try {
            Boolean isLock = redisLockUtil.lock(lockKey, lockValue, redisLock.expire(), redisLock.timeUnit());
            logger.info("{} 获得锁的结果是 {} ", redisLock.key(), isLock);
            if (!isLock) {
                // 获得锁失败
                logger.debug("获得锁失败 {}", redisLock.key());
                // 可以自定一个异常类和其拦截器，见 Spring Boot 2.X 实战--RESTful API 全局异常处理
                // https://ylooq.gitee.io/learn-spring-boot-2/#/09-ErrorController?id=spring-boot-2x-%e5%ae%9e%e6%88%98-restful-api-%e5%85%a8%e5%b1%80%e5%bc%82%e5%b8%b8%e5%a4%84%e7%90%86
                // 或者 @AfterThrowing:  异常抛出增强，相当于ThrowsAdvice
                throw new RuntimeException("获取锁失败");
            } else {
                try {
                    // 进行处理
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException("系统异常");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("系统异常");
        }
    }


    @After(value = "pointcut()")
    public void after() {
        // 释放锁
        if (redisLockUtil.unlock(lockKey, lockValue)) {
            logger.error("redis分布式锁解锁异常 key 为 {}", lockKey);
        }
    }
}
