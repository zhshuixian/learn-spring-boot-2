package org.xian.mongo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;


@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserRepositoryTest {

    @Resource
    MongoTemplate mongoTemplate;

    @Resource
    UserRepository userRepository;

    @Test
    public void insert() {
        User user = new User();
        user.setId(1);
        user.setUsername("MongoDB");
        user.setNickname("开源，跨平台，面向文档的数据库");
        userRepository.insert(user);
        // 使用 MongoTemplate
        user.setId(2);
        user.setUsername("Redis");
        user.setNickname("内存中的数据结构存储系统");
        mongoTemplate.insert(user);
    }

    @Test
    public void select() {
        System.out.println("查询所有的记录");
        // 方式一
        System.out.println(userRepository.findAll());
        // 方式二
        System.out.println(mongoTemplate.findAll(User.class));
        // 查询某个记录，根据 JPA 方法名命名规则
        System.out.println("根据条件查询 userRepository");
        System.out.println(userRepository.findByUsernameContains("go"));
        System.out.println(userRepository.findByNicknameContaining("内存"));
        // 在 userRepository 自定义查询语句，使用 MongoDB 的查询方式
        System.out.println(userRepository.mySelect("存储系统"));
        // 使用 mongoTemplate
        System.out.println("根据条件查询 mongoTemplate");
        // id = 1
        System.out.println(mongoTemplate.findById(2, User.class));
        // 等同于 SQL 的 where id = 1 and username like %DB%
        Query query = new Query(Criteria.where("id").is(1).and("username").regex("DB"));
        System.out.println(mongoTemplate.find(query, User.class));
    }

    @Test
    public void update() {
        // 使用 userRepository 更新
        User user = userRepository.findByIdIs(2);
        // 更新 Nickname
        user.setNickname("Redis 开源非关系数据库");
        // save 跟 insert 差不多，不同在于 save 如果存在 ID 值了就更新，还不存在该 ID 值就插入
        userRepository.save(user);
        System.out.println("userRepository 更新数据结果");
        System.out.println(userRepository.findByIdIs(2));

        // 使用 mongoTemplate
        Query query = new Query(Criteria.where("id").is(1));
        Update update = new Update();
        update.set("nickname", "MongoDB 流行的文档数据库 更新：By mongoTemplate");
        mongoTemplate.updateFirst(query, update, User.class);
        System.out.println("mongoTemplate 更新数据结果");
        System.out.println(mongoTemplate.findById(1, User.class));
    }

    @Test
    public void delete() {
        // 使用 userRepository
        userRepository.deleteById(1);
        // 使用 mongoTemplate
        Query query = new Query(Criteria.where("id").is(1));
        // 找到符合条件的然后移除
        mongoTemplate.findAndRemove(query, User.class);
        // 删除所有 user 集合(Collection) 的记录
        userRepository.deleteAll();
        // 删除所有 user 集合(Collection) 的记录
        mongoTemplate.remove(User.class);
    }

}