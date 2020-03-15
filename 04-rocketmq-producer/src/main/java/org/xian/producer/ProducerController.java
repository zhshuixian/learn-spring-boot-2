package org.xian.producer;

import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author xian
 */
@RestController
@RequestMapping("/producer")
public class ProducerController {
    @Resource ProducerService producerService;

    @PostMapping("/string")
    public SendResult sendString(@RequestBody String message){
        return producerService.sendString(message);
    }

    @PostMapping("/user")
    public SendResult sendUser(@RequestBody User user){
        return producerService.sendUser(user);
    }

}
