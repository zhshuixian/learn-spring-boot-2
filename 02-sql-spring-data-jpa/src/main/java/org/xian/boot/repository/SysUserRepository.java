package org.xian.boot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.xian.boot.entity.SysUser;


/**
 * @author xian
 * sys_user 表数据持久层
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser findByUsername(String username);

    /**
     * 根据用户昵称查询用户信息
     *
     * @param nickname 用户昵称
     * @param pageable 分页
     * @return 用户信息
     */
    @Query("SELECT sysUser  from SysUser sysUser where sysUser.nickname like %:nickname%")
    Page<SysUser> searchByNickname(@Param("nickname") String nickname, Pageable pageable);

    /**
     * 根据用户昵称查询用户信息
     *
     * @param nickname 用户昵称
     * @param pageable 分页
     * @return 用户信息
     */
    Page<SysUser> findByNicknameLike(@Param("nickname") String nickname, Pageable pageable);



}
