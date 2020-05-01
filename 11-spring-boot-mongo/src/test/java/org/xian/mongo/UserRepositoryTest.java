package org.xian.mongo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
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
    public void contextLoads() {


    }

    @Test
    public void insertRepo() {
        User user = new User();
        user.setId(1);
        user.setUsername("april");
        user.setNickname("四月");
        userRepository.insert(user);
    }


}