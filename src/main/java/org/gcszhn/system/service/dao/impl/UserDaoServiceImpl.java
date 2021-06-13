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
package org.gcszhn.system.service.dao.impl;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.security.RandomUtilis;
import org.gcszhn.system.security.ShaEncrypt;
import org.gcszhn.system.service.dao.UserDaoService;
import org.gcszhn.system.service.exception.UDException;
import org.gcszhn.system.service.user.User;
import org.gcszhn.system.service.user.UserNode;
import org.gcszhn.system.service.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;

/**
 * 用户数据库操作DAO接口的实现类
 * @author Zhang.H.N
 * @version 1.0
 */
@Repository
public class UserDaoServiceImpl implements UserDaoService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Setter @Value("${spring.datasource.table}")
    private String table;
    @Transactional
    @Override
    public void addUser(User user) {
        try { 
            String sql = "INSERT INTO "+table+
            " SET username=?,password=?,nodeconfig=?,address=?,role=?,owner=?";
            jdbcTemplate.update(sql,
                user.getAccount(),
                encryptPassword(user.getPassword()),
                JSON.toJSONString(user.getNodeConfigs()),
                user.getAddress(),
                user.getUseRole().toString(),
                user.getOwner()
                );
            AppLog.printMessage("Register to database successfully");
        } catch (Exception e) {
            AppLog.printMessage("Register to database failed", Level.ERROR);
            throw new UDException(e);
        }
    }
    @Transactional
    @Override
    public void removeUser(User user) {
        try {
            String sql = "DELETE FROM "+table+" WHERE username=?";
            jdbcTemplate.update(sql, user.getAccount());
            AppLog.printMessage("Remove from database successfully");
        } catch (Exception e) {
            AppLog.printMessage("Remove from database failed", Level.ERROR);
            throw new UDException(e);
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    public int verifyUser(User user) {
        try {
            String sql = "SELECT * FROM "+table+" WHERE username=?";
            List<Map<String, Object>> rs = jdbcTemplate.queryForList(sql, user.getAccount());
            String secret = null;
            String address = null;
            UserRole role = null;
            Timestamp createTime = null;
            Timestamp lastLoginTime = null;
            Object nodeset = null;
            if (rs.size()==1) {
                secret = (String) rs.get(0).get("password");
                address = (String) rs.get(0).get("address");
                if (!user.getAccount().equals("root")) {
                    nodeset = rs.get(0).get("nodeconfig");
                }
                role = UserRole.valueOf((String) rs.get(0).get("role"));
                createTime =(Timestamp) rs.get(0).get("create_stamp");
                lastLoginTime = (Timestamp) rs.get(0).get("last_login_stamp");
            } else if (rs.isEmpty()) {
                AppLog.printMessage("User not found!", Level.ERROR);
                return -1;
            } else {
                AppLog.printMessage("Database error: repeat account!", Level.ERROR);
                return 2;
            }
            ;
            String[] arr = secret.split(":");
            if (arr.length == 1) {
                AppLog.printMessage("Database error: ilegal password", Level.ERROR);
                return 2;
            }
            // 密码正确时才更新节点信息给当前对象
            if (ShaEncrypt.encrypt(user.getPassword(), arr[0]).equals(arr[1])) {
                AppLog.printMessage("Authentication successfully!");
                user.setUseRole(role);
                user.setCreateTime(createTime);
                user.setLastLoginTime(lastLoginTime);
                // root用户是管理员，没有节点信息
                if (!user.getAccount().equals("root")) {
                    user.setAddress(address);
                    if (nodeset instanceof ArrayList) {//序列化对象
                        user.setNodeConfigs((ArrayList<UserNode>) nodeset);
                    } else if (nodeset instanceof byte[]) {//历史遗留数据格式
                        Object nodeset1 = getObjectFromBytes((byte[]) nodeset);
                        if (nodeset1 == null) {
                            JSONArray jsonArray = JSONArray.parseArray(new String((byte[])nodeset));
                            ArrayList<UserNode> newSets = new ArrayList<>(jsonArray.size());
                            for (int i = 0; i < jsonArray.size(); i++) {
                                newSets.add(UserNode.getUserNodeFromJSON(jsonArray.getJSONObject(i)));
                            }
                            user.setNodeConfigs(newSets);
                        } else {
                            user.setNodeConfigs((ArrayList<UserNode>) nodeset1);
                        }
                    } else if (nodeset instanceof String) {
                        JSONArray jsonArray = JSONArray.parseArray((String)nodeset);
                        ArrayList<UserNode> newSets = new ArrayList<>(jsonArray.size());
                        for (int i = 0; i < jsonArray.size(); i++) {
                            newSets.add(UserNode.getUserNodeFromJSON(jsonArray.getJSONObject(i)));
                        }
                        user.setNodeConfigs(newSets);
                    } else {
                       throw new UDException(new Exception("Ilegal Node Format!"));
                    }
                }
                return 0;
            } else {
                AppLog.printMessage("Authentication failed!", Level.ERROR);
                return 1;
            }
        } catch (Exception e) {
            throw new UDException(e);
        }
    }
    @Transactional
    @Override
    public void updateUser(User user) {
        try {
            String sql = "UPDATE "+table+
            " SET password=?,nodeconfig=?,address=?,role=?,owner=?,last_login_stamp=? WHERE username=?";
            jdbcTemplate.update(sql,
                encryptPassword(user.getPassword()),
                JSON.toJSONString(user.getNodeConfigs()),
                user.getAddress(),
                user.getUseRole().toString(),
                user.getOwner(),
                user.getLastLoginTime(),
                user.getAccount()
            );
            AppLog.printMessage("Update user info successfully!");
        } catch (Exception e) {
            AppLog.printMessage("Update user info failed!", Level.ERROR);
            throw new UDException(e);
        }
    }
    @Override
    public List<User> fetchUserList() {
        String sql = "SELECT username,nodeconfig,address FROM " +table;
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        List<User> res = new ArrayList<>(result.size());
        result.forEach((Map<String, Object> mp)->{
            if (mp != null) {
                User user = new User();
                res.add(user);
                user.setAccount((String)mp.get("username"));
                user.setAddress((String)mp.get("address"));
                String jsonString = (String)mp.get("nodeconfig");
                if (jsonString != null && !jsonString.equals("")) {
                    JSONArray jsonArray = JSONArray.parseArray(jsonString);
                    ArrayList<UserNode> newSets = new ArrayList<>(jsonArray.size());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        newSets.add(UserNode.getUserNodeFromJSON(jsonArray.getJSONObject(i)));
                    }
                    user.setNodeConfigs(newSets);
                }
            }
        });
        return res;
    }
    /**
     * 将密码生成带盐SHA256密文
     * @param passwd 密码明文
     * @return 带盐SHA256密文
     */
    public static String encryptPassword(String passwd) {
        String salt = RandomUtilis.getSalt(15);
        String enc_pass = ShaEncrypt.encrypt(passwd, salt);
        return salt + ":" + enc_pass;
    }
    /**
     * 从字节数组中反序列化对象，用于支持旧版数据储存格式
     * @param arr 字节数组输入
     * @return 对象
     */
    public static Object getObjectFromBytes(byte[] arr) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(arr));
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
