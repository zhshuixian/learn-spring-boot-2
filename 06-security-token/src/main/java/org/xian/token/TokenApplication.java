package org.xian.token;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author xian
 */
@SpringBootApplication
@MapperScan("org.xian.token.mapper")
public class TokenApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokenApplication.class, args);
    }

}
