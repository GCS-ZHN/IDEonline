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
package org.gcszhn.system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;

/**APP中专门处理与Redis服务器交互的工具
 * @author Zhang.H.N
 * @version 1.0
 */
@Service
public class RedisOperation {
    /**默认主机配置 */
    private static String DEFAULT_HOST;
    /**默认端口配置 */
    private static int DEFAULT_PORT;
    /**默认密码配置 */
    private static String DEFAULT_PASS;
    /**
     * 初始化Redis主机
     * @param host 配置的主机
     */
    @Value("${redis.host}")
    public void setHost(String host) {
        RedisOperation.DEFAULT_HOST = host;
    }
    /**
     * 初始化Redis端口
     * @param port 配置的端口
     */
    @Value("${redis.port}")
    public void setPort(int port) {
        RedisOperation.DEFAULT_PORT = port;
    }
    /**
     * 初始化Redis密码
     * @param passwd 配置的密码
     */
    @Value("${redis.password}")
    public void setPassword(String passwd) {
        RedisOperation.DEFAULT_PASS = passwd;
    }
    /**
     * 连接指定主机指定端口的Redis服务器
     * @param host 主机IP或域名
     * @param port Redis服务端口
     * @param passwd 密码
     * @return Redis连接对象
     */
    public static Jedis getRedis(String host, int port, String passwd) { 
        JedisShardInfo jsi = new JedisShardInfo(host, port);
        if (passwd != null) jsi.setPassword(passwd);
        Jedis jedis = new Jedis(jsi);
        return jedis;
    }
    /**
     * 连接指定主机的Redis服务器
     * @param host 主机IP或域名
     * @param passwd 密码
     * @return Redis连接对象
     */
    public static Jedis getRedis(String host, String passwd) {
        return getRedis(host, 6379, passwd);
    }
    /**
     * 连接本地Redis服务器
     * @param passwd 密码
     * @return Redis连接对象
     */
    public static Jedis getRedis(String passwd) {
        return getRedis("localhost", passwd);
    }
    /**
     * 为哈希表添加字段
     * @param key 哈希表键值
     * @param field 字段
     * @param value 值
     */
    public static void redisHset(String key, String field, String value) {
        Jedis jedis = getRedis(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PASS);
        jedis.hset(key, field, value);
        jedis.close();
    }
    /**
     * 获取哈希表的指定字段
     * @param key 哈希表的键值
     * @param field 字段
     * @return
     */
    public static String redisHget(String key, String field) {
        Jedis jedis = getRedis(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PASS);
        String value = jedis.hget(key, field);
        jedis.close();
        return value;
    }
    /**
     * 删除redis哈希表的指定字段
     * @param key 哈希表的键值，即哪个哈希表
     * @param field 字段
     */
    public static void redisHdel(String key, String field) {
        Jedis jedis = getRedis(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PASS);
        jedis.hdel(key, field);
        jedis.close();
    }
}