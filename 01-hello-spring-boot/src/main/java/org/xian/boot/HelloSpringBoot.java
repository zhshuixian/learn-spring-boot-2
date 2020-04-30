package org.xian.boot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author xian
 */
@RestController
@RequestMapping("hello")
@Slf4j
public class HelloSpringBoot {

    @RequestMapping("string")
    @ResponseStatus(HttpStatus.OK)
    public String helloString(){
        log.trace("trace");
        log.debug("debug");
        log.warn("warn");
        log.info("info");
        log.error("error");
        return "Hello Spring Boot";
    }

    @RequestMapping(value = "json")
    public Messages helloJson(){
        return new Messages("success","Hello Spring Boot By JSON");
    }

    @GetMapping(value = "param")
        public Messages param(@RequestParam(value ="username",defaultValue = "Boot") String username){
        return new Messages("success","Hello "+ username);
    }

    @PostMapping(value = "path/{username}")
    public Messages pathVariable(@PathVariable("username") String username){
        return new Messages("success","Hello "+ username);
    }

    @PostMapping("body")
    public Messages body(@RequestBody SysUser sysUser){
        Messages messages = new Messages();
        // 需要注意 Null PointerException
        if(sysUser.getUsername() !=null && sysUser.getPassword() !=null &&
                sysUser.getUsername().equals("user") && sysUser.getPassword().equals("springboot")){
            messages.setStatus("success");
            messages.setMessages("Login  Success");
        }else {
            messages.setStatus("error");
            messages.setMessages("Login  Error");
        }
        return messages;
    }

    @PostMapping("text")
    public String text(@RequestBody String  text){
        return text;
    }

    @PostMapping("form")
    public Messages form(SysUser sysUser){
        Messages messages = new Messages();
        if(sysUser.getUsername() !=null && sysUser.getPassword() !=null &&
                sysUser.getUsername().equals("user") && sysUser.getPassword().equals("springboot")){
            messages.setStatus("success");
            messages.setMessages("Login  Success");
        }else {
            messages.setStatus("error");
            messages.setMessages("Login  Error");
        }
        return messages;
    }
}
