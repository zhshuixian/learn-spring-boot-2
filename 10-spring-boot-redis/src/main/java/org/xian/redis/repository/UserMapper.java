package org.xian.redis.repository;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.xian.redis.entity.User;

/**
 * @author xiaoxian
 */
public interface UserMapper {
    /**
     * 根据用户名 查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("Select username, age From user Where username=#{username}")
    User selectByUsername(String username);

    /**
     * 根据用户名更新用户昵称、用户年龄、用户性别 信息
     *
     * @param user 用户信息
     */
    @Update("Update user Set age=#{age} Where username=#{username}")
    void update(User user);


    /**
     * 根据用户名 删除用户信息
     *
     * @param username 用户名
     */
    @Delete("Delete From user where username=#{username}")
    void delete(String username);

}
