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

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

/**
 * APP中专门处理与Redis服务器交互的工具
 * @author Zhang.H.N
 * @version 1.0
 */
public interface RedisService {
    /**
     * 初始化Redis主机
     * @param host 配置的主机
     */
    public void setHost(String host);
    /**
     * 初始化Redis端口
     * @param port 配置的端口
     */
    public void setPort(int port);
    /**
     * 初始化Redis密码
     * @param passwd 配置的密码
     */
    public void setPassword(String passwd);
    /**
     * 连接指定主机指定端口的Redis服务器
     * @param host 主机IP或域名
     * @param port Redis服务端口
     * @param passwd 密码
     * @return Redis连接对象
     */
    public Jedis getRedis(String host, int port, String passwd);
    /**
     * 连接指定主机的Redis服务器
     * @param host 主机IP或域名
     * @param passwd 密码
     * @return Redis连接对象
     */
    public Jedis getRedis(String host, String passwd);
    /**
     * 连接本地Redis服务器
     * @param passwd 密码
     * @return Redis连接对象
     */
    public Jedis getRedis(String passwd);
    /**
     * 为哈希表添加字段
     * @param key 哈希表键值
     * @param field 字段
     * @param value 值
     */
    public void redisHset(String key, String field, String value);
    /**
     * 获取哈希表的指定字段
     * @param key 哈希表的键值
     * @param field 字段
     * @return
     */
    public String redisHget(String key, String field);
    /**
     * 删除redis哈希表的指定字段
     * @param key 哈希表的键值，即哪个哈希表
     * @param field 字段
     */
    public void redisHdel(String key, String field);
    /**
     * 获取redis哈希表的字段集合
     * @param key 哈希表的键
     * @return 键的集合
     */
    public Set<String> redisFields(String key);
    /**
     * 获取redis哈希表的值集合
     * @param key 哈希表的键
     * @return 值的列表
     */
    public List<String> redisValues(String key);
    /**
     * 获取redis哈希表的全部内容
     * @param key 指定的哈希表键
     * @return Map对象
     */
    public Map<String, String> redisHgetAll(String key);
    /**
     * 删除redis指定键值对
     * @param keys 键
     */
    public void redisDel(String... keys);
    /**
     * 添加redis键值对
     * @param key 键
     * @param value 值
     */
    public void redisSet(String key, String value);
    /**
     * 获取指定redis键的值
     * @param key 键
     * @return 值
     */
    public String redisGet(String key);
}