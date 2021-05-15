/*
 * Copyright © 2021 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 *
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 *
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */
package org.gcszhn.system.service.impl;
import org.gcszhn.system.service.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;

/**
 * Redis服务的接口扩展
 * @author Zhang.H.N
 * @version 1.0
 */
@Repository
public class RedisServiceImpl implements RedisService {
    /**默认主机配置 */
    private static String DEFAULT_HOST;
    /**默认端口配置 */
    private static int DEFAULT_PORT;
    /**默认密码配置 */
    private static String DEFAULT_PASS;
    @Override
    @Value("${redis.host}")
    public void setHost(String host) {
        RedisServiceImpl.DEFAULT_HOST = host;
    }
    @Override
    @Value("${redis.port}")
    public void setPort(int port) {
        RedisServiceImpl.DEFAULT_PORT = port;
    }
    @Override
    @Value("${redis.password}")
    public void setPassword(String passwd) {
        RedisServiceImpl.DEFAULT_PASS = passwd;
    }
    @Override
    public Jedis getRedis(String host, int port, String passwd) { 
        JedisShardInfo jsi = new JedisShardInfo(host, port);
        if (passwd != null) jsi.setPassword(passwd);
        Jedis jedis = new Jedis(jsi);
        return jedis;
    }
    @Override
    public Jedis getRedis(String host, String passwd) {
        return getRedis(host, 6379, passwd);
    }
    @Override
    public Jedis getRedis(String passwd) {
        return getRedis("localhost", passwd);
    }
    @Override
    public void redisHset(String key, String field, String value) {
        Jedis jedis = getRedis(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PASS);
        jedis.hset(key, field, value);
        jedis.close();
    }
    @Override
    public String redisHget(String key, String field) {
        Jedis jedis = getRedis(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PASS);
        String value = jedis.hget(key, field);
        jedis.close();
        return value;
    }
    @Override
    public void redisHdel(String key, String field) {
        Jedis jedis = getRedis(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PASS);
        jedis.hdel(key, field);
        jedis.close();
    }
}