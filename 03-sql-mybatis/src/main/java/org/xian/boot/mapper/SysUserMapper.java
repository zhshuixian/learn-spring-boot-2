package org.xian.boot.mapper;

import org.apache.ibatis.annotations.*;
import org.xian.boot.entity.SysUser;

import java.util.List;

/**
 * @author xian
 */
public interface SysUserMapper {
    /**
     * 往 sys_user 插入一条记录
     *
     * @param sysUser 用户信息
     */
    @Insert("Insert Into sys_user(username, nickname, user_age, user_sex) " +
            "Values(#{username}, #{nickname}, #{userAge}, #{userSex})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insert(SysUser sysUser);
    void insertOnXml(SysUser sysUser);


    /**
     * 根据用户 ID 查询用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    @Select("Select user_id,username, nickname, user_age, user_sex From sys_user Where user_id=#{userId}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "userAge", column = "user_age"),
            @Result(property = "userSex", column = "user_sex")
    })
    SysUser selectByUserId(Long userId);
    SysUser selectByUserIdOnXml(Long userId);

    /**
     * 根据用户名更新用户昵称、用户年龄、用户性别 信息
     *
     * @param sysUser 用户信息
     */
    @Update("Update sys_user Set nickname=#{nickname}, user_age=#{userAge}, user_sex=#{userSex} Where username=#{username}")
    void update(SysUser sysUser);
    void updateOnXml(SysUser sysUser);


    /**
     * 根据用户 ID 删除用户信息
     *
     * @param userId 用户 ID
     */
    @Delete("Delete From sys_user where user_id=#{userId}")
    void delete(Long userId);
    void deleteOnXml(Long userId);

    /**
     * 浏览所有用户信息
     *
     * @return 所有用户信息
     */
    @Select("Select * From sys_user")
    List<SysUser> selectAll();
    List<SysUser> selectAllOnXml();

}
