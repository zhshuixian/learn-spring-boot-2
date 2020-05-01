package org.xian.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author xiaoxian
 */
@Repository
public interface UserRepository extends MongoRepository<User, Integer> {

}
