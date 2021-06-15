package org.gcszhn;

import org.gcszhn.system.service.redis.RedisService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Redis服务的测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class RedisServiceTest extends AppTest {
    /**
     * Redis连接测试
     */
    @Autowired
    RedisService redisService;
    @Test
    public void testRedis() {
        redisService.redisHset("session", "session12", "this is java test for redis");
        System.out.println(redisService.redisHget("session", "session12"));
        redisService.redisHdel("session", "session12");
    }
}