package org.xian.boot;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xian
 */
@RestController
public class HelloSpringBoot {
    @RequestMapping("/hello/string")
    public String helloString(){
        return "Hello Spring Boot";
    }

    @RequestMapping("/hello/json")
    public Messages helloJson(){
        return new Messages("success","Hello Spring Boot By JSON");
    }
}
