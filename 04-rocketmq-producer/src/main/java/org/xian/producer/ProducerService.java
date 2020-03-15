package org.xian.producer;


import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author xian
 */
@Service
public class ProducerService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private RocketMQTemplate mqTemplate;

    @Value(value = "${boot.rocketmq.topic}")
    private String springTopic;

    @Value(value = "${boot.rocketmq.topic.user}")
    private String userTopic;

    @Value(value = "${boot.rocketmq.tag}")
    private String tag;

    public SendResult sendString(String message) {
        // Send string
        SendResult sendResult = mqTemplate.syncSend(springTopic + ":" + tag, message);
        logger.info("syncSend1 to topic {} sendResult={} \n", springTopic, sendResult);
        return sendResult;
    }

    public SendResult sendUser(User user) {
        SendResult sendResult = mqTemplate.syncSend(userTopic, user);
        logger.info("syncSend1 to topic {} sendResult= {} \n", userTopic, sendResult);
        return sendResult;
    }
}
