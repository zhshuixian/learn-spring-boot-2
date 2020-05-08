package org.xian.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaoxian
 */
@RestController
public class LockController {

    @Resource
    RedisLockUtil redisLockUtil;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/buy")
    public String buy(@RequestParam String goodId) {
        long timeout = 15;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        String lockValue = UUID.randomUUID().toString();
        if (redisLockUtil.lock(goodId, lockValue, timeout, timeUnit)) {
            // 业务处理
            logger.info("获得锁，进行业务处理");
            try {
                // 休眠 10 秒钟
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 释放锁
            if (redisLockUtil.unlock(goodId, lockValue)) {
                logger.error("redis分布式锁解锁异常 key 为" + goodId);
            }
            return "购买成功";

        }
        return "请稍后再试";
    }


    @RequestMapping("/buybuybuy")
    @RedisLock(key = "lock_key", value = "lock_value")
    public String buybuybuy(@RequestParam(value = "goodId") String goodId) {
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "购买成功";
    }
}
