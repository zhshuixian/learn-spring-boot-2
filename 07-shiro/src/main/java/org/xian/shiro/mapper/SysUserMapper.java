package org.xian.shiro.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import org.xian.shiro.entity.SysUser;


/**
 * @author xian
 */
public interface SysUserMapper {
    /**
     * 往 sys_user 插入一条记录
     *
     * @param sysUser 用户信息
     */
    @Insert("Insert Into sys_user(username, password,user_role,user_permission) Values(#{username}, #{password},#{userRole},#{userPermission})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insert(SysUser sysUser);

    /**
     * 根据用户 Username 查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("Select user_id,username, password,user_role,user_permission From sys_user Where username=#{username}")
    SysUser selectByUsername(String username);
}
