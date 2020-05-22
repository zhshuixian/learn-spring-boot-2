package org.xian.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaoxian
 */
@Configuration
// Endpoint 指定端点 Id
@Endpoint(id = "my-endpoint")
public class MyEndpoint {
    @ReadOperation
    public Map<String, Object> endpoint() {
        Map<String, Object> map = new HashMap<>(2);
        // 将需要监控的信息写入 map 然后返回
        map.put("status", "成功");
        map.put("message", "这是自定义的端点");
        return map;
    }
}