package org.xian.redis.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xian.redis.entity.User;

/**
 * @author xiaoxian
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    /**
     * 根据用户查询
     *
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(@Param("username") String username);
}
