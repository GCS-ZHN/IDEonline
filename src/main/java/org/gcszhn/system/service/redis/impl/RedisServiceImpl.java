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
package org.gcszhn.system.service.redis.impl;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisShardInfo;

/**
 * Redis服务的接口扩展
 * @author Zhang.H.N
 * @version 1.2
 */
@Repository
public class RedisServiceImpl implements RedisService {
    /**默认主机配置 */
    @Value("${redis.host}")
    private String host;
    /**默认端口配置 */
    @Value("${redis.port}")
    private int port;
    /**默认密码配置 */
    @Value("${redis.password}")
    private String pass;
    /**连接超时配置 */
    @Value("${redis.timeout}")
    private int timeout;
    /**数据库连接池 */
    JedisPool jedisPool;
    @Autowired
    public void setJedisPool(Environment env) {
        GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(env.getProperty("redis.maxTotal", Integer.class));           //最大连接数
        poolConfig.setMaxIdle(env.getProperty("redis.maxIdle", Integer.class));             //最大空闲数
        poolConfig.setMaxWaitMillis(env.getProperty("redis.maxWaitMillis", Integer.class)); //最大等待资源时间
        poolConfig.setJmxEnabled(env.getProperty("redis.jmxEnabled", Boolean.class));       //开启JMX监控
        jedisPool = new JedisPool(poolConfig, host, port, timeout, pass);
        AppLog.printMessage("Initialize jedis pool");
    }
    public Jedis getRedis() {
        Jedis jedis = jedisPool.getResource();
        AppLog.printMessage(String.format(
            "Jedis pool status: active:%d, idle:%d, wait:%d, mean wait %d ms, max wait %d ms",
            jedisPool.getNumActive(),
            jedisPool.getNumIdle(),
            jedisPool.getNumWaiters(),
            jedisPool.getMeanBorrowWaitTimeMillis(),
            jedisPool.getMaxBorrowWaitTimeMillis()));
        return jedis;
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
        Jedis jedis = null; 
        try {
            jedis = getRedis();    //获取连接
            jedis.hset(key, field, value);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } finally {
            if (jedis!=null) jedis.close();     //使用数据池，则归还给数据池，否则关闭TCP连接
        }
    }
    @Override
    public String redisHget(String key, String field) {
        String value = null;
        Jedis jedis = null; 
        try {
            jedis = getRedis();    //获取连接
            value = jedis.hget(key, field);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } finally {
            if (jedis!=null) jedis.close();     //使用数据池，则归还给数据池，否则关闭TCP连接
        }
        return value;
    }
    @Override
    public void redisHdel(String key, String field) {
        Jedis jedis = null; 
        try {
            jedis = getRedis();    //获取连接
            jedis.hdel(key, field);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } finally {
            if (jedis!=null) jedis.close();     //使用数据池，则归还给数据池，否则关闭TCP连接
        }
    }
    @Override
    public Set<String> redisFields(String key) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = getRedis();    //获取连接
            res = jedis.hkeys(key);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } finally {
            if (jedis!=null) jedis.close();     //使用数据池，则归还给数据池，否则关闭TCP连接
        }
        return res;
    }
    @Override
    public List<String> redisValues(String key) {
        Jedis jedis = null;
        List<String> res = null; 
        try {
            jedis = getRedis();    //获取连接
            res = jedis.hvals(key);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } finally {
            if (jedis!=null) jedis.close();     //使用数据池，则归还给数据池，否则关闭TCP连接
        }
        return res;
    }
    @Override
    public Map<String, String> redisHgetAll(String key) {
        Jedis jedis = null;
        Map<String, String> res = null;
        try {
            jedis = getRedis();    //获取连接
            res = jedis.hgetAll(key);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } finally {
            if (jedis!=null) jedis.close();     //使用数据池，则归还给数据池，否则关闭TCP连接
        }
        return res;
    }
    @Override
    public void redisDel(String... keys) {
        Jedis jedis = null; 
        try {
            jedis = getRedis();    //获取连接
            jedis.del(keys);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } finally {
            if (jedis!=null) jedis.close();     //使用数据池，则归还给数据池，否则关闭TCP连接
        }
    }
    @Override
    public void redisSet(String key, String value) {
        Jedis jedis = null; 
        try {
            jedis = getRedis();    //获取连接
            jedis.set(key, value);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } finally {
            if (jedis!=null) jedis.close();     //使用数据池，则归还给数据池，否则关闭TCP连接
        }
    }
    @Override
    public String redisGet(String key) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = getRedis();    //获取连接
            res = jedis.get(key);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } finally {
            if (jedis!=null) jedis.close();     //使用数据池，则归还给数据池，否则关闭TCP连接
        }
        return res;
    }
}