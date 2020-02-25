package org.xian.boot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xian.boot.entity.SysUser;

/**
 * @author xian
 * sys_user 表数据持久层
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser,Long> {
}
