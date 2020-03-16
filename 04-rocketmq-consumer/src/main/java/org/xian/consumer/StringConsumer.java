package org.xian.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * StringConsumer 接受 String 类型的消息
 *
 * @author xian
 */
@Service
@RocketMQMessageListener(topic = "${boot.rocketmq.topic}", consumerGroup = "string_consumer", selectorExpression = "${boot.rocketmq.tag}")
public class StringConsumer implements RocketMQListener<String> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onMessage(String message) {
        // 重写消息处理方法
        logger.info("------- StringConsumer received:{} \n", message);
        // TODO 对消息进行处理，比如写入数据
    }
}