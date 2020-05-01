package org.xian.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author xiaoxian
 */
@Repository
public interface UserRepository extends MongoRepository<User, Integer> {
    /**
     * username 包含传入参数的所有文档
     *
     * @param username username
     * @return 所有符合条件的文档
     */
    List<User> findByUsernameContains(String username);


    /**
     * nickname 包含传入参数的所有文档
     *
     * @param nickname nickname
     * @return 所有符合条件的文档
     */
    List<User> findByNicknameContaining(String nickname);

    /**
     * id 等于传入值的记录，如果有多条，则返回第一条
     *
     * @param id id 值
     * @return 第一条符合条件的文档
     */
    User findByIdIs(Integer id);


    /**
     * 自定义查询，语法使用 MongoDB 的查询语言
     *
     * @param nickname nickname
     * @return nickname 包含传入参数的所有文档
     */
    @Query("{nickname:{$regex:?0}}}")
    List<User> mySelect(String nickname);
}
