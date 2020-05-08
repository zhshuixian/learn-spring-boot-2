package org.xian.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 进行加锁与解锁
 *
 * @author xiaoxian
 */
@Component
public class RedisLockUtil {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    public boolean lock(String key, String value, long timeout, TimeUnit timeUnit) {

/*        // long timeout = 100000;
        // TimeUnit timeUnit = TimeUnit.SECONDS;
        logger.info("开始获得锁 key =" + value);
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, 15, TimeUnit.SECONDS);
        return result != null && result;*/

        Boolean lockStat = stringRedisTemplate.execute((RedisCallback<Boolean>) connection ->
                connection.set(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8),
                        Expiration.from(timeout, timeUnit), RedisStringCommands.SetOption.SET_IF_ABSENT));
        return lockStat != null && lockStat;
    }

    public boolean unlock(String key, String value) {

/* 不要使用这种方式
        try {
            String redisValue = stringRedisTemplate.opsForValue().get(key);
            if (redisValue != null && redisValue.equals(value)) {
                // 释放锁
                stringRedisTemplate.opsForValue().getOperations().delete(key);
            }
            return true;
        } catch (Exception e) {
            logger.error("解锁失败");
        }
*/

        try {
            // 使用 lua 脚本
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Boolean unLockStat = stringRedisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.eval(script.getBytes(), ReturnType.BOOLEAN, 1,
                            key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8)));

            return unLockStat == null || !unLockStat;
        } catch (Exception e) {
            logger.error("解锁失败 key = {}", key);
            return false;
        }
    }


}
