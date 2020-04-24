package org.xian.redis.controller;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xian.redis.entity.User;
import org.xian.redis.repository.UserMapper;
import org.xian.redis.repository.UserRepository;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * @author xiaoxian
 */
@RestController
@CacheConfig(cacheNames = "users")
public class RedisController {
    @Resource
    private StringRedisTemplate stringTemplate;
    @Resource
    private RedisTemplate<String, User> redisTemplate;
    @Resource
    private UserRepository userRepository;
    @Resource
    private UserMapper userMapper;

    @RequestMapping("/setString")
    public String setString(@RequestParam(value = "key") String key,
                            @RequestParam(value = "value") String value) {
        stringTemplate.opsForValue().set(key, value);
        return "ok";
    }

    @RequestMapping("/getString")
    public String getString(@RequestParam(value = "key") String key) {
        return stringTemplate.opsForValue().get(key);
    }
    // TODO User 类存入和时间限制

    @RequestMapping(value = "/setUser")
    public String setUser(@RequestBody User user) {
        redisTemplate.opsForValue().set(user.getUsername(), user, Duration.ofMinutes(1));
        return "ok";
    }

    @RequestMapping("/getUser")
    public User getUser(@RequestParam(value = "key") String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @RequestMapping("/delUser")
    public User delUser(@RequestParam(value = "key") String key) {
        User user = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        return user;
    }
    // TODO 数据库缓存

    @RequestMapping("/select")
    @Cacheable(key = "#username")
    public User select(@RequestParam(value = "username") String username) {
        return userRepository.findByUsername(username);
    }

    @RequestMapping("/update")
    @CachePut(key = "#user.username")
    public User update(@RequestBody User user) {
        return userRepository.save(user);
    }

    @RequestMapping("/delete")
    @CacheEvict(key = "#username")
    public User delete(@RequestParam(value = "username") String username) {
        User user = select(username);
        userRepository.delete(user);
        return user;
    }

    @RequestMapping("/deleteAllCache")
    @CacheEvict(allEntries = true)
    public String deleteAllCache() {
        // 删除所有缓存
        return "OK";
    }

    @RequestMapping("/mySelect")
    @Cacheable(value = "users", key = "#username")
    public User mySelect(@RequestParam(value = "username") String username) {
        return userMapper.selectByUsername(username);
    }

    @RequestMapping("/myUpdate")
    @CachePut(value = "users", key = "#user.username")
    public User myUpdate(@RequestBody User user) {
        userMapper.update(user);
        return userMapper.selectByUsername(user.getUsername());
    }

    @RequestMapping("/myDelete")
    @CacheEvict(value = "users", key = "#username")
    public User myDelete(@RequestParam(value = "username") String username) {
        userMapper.delete(username);
        return userMapper.selectByUsername(username);
    }

}
