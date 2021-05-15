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
package org.gcszhn.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.gcszhn.system.security.RSAEncrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

/**
 * 用于获取RSA加密公钥的API
 * @author Zhang.H.N
 * @version 1.0
 */
@RestController
public class PublicKey {
    /**返回key的数据结构 */
    @Data
    public class KeyJSON {
        private String key;
    }
    /**HTTP请求对象 */
    @Autowired
    HttpServletRequest request;
    /**处理请求的方法 */
    @GetMapping("/getkey")
    public KeyJSON doGet() {
        request.getSession().invalidate();
        HttpSession session = request.getSession();
        //最多闲置10分钟不使用，一旦通过验证立刻失效
        session.setMaxInactiveInterval(600);
        String[] keypairs = new String[2];
        RSAEncrypt.generateKeyPair(keypairs);
        session.setAttribute("keypair", keypairs);
        KeyJSON kj = new KeyJSON();
        kj.setKey(keypairs[1]);
        return kj;
    }
}