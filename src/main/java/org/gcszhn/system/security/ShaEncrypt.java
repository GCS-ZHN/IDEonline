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
package org.gcszhn.system.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.gcszhn.system.config.JSONConfig;
/**
 * SHA单向加密算法
 * @author Zhang.H.N
 * @version 1.0
 */
public class ShaEncrypt {
    /**
     * SHA-256的加盐加密算法
     * @param message UTF-8编码的明文
     * @param salt UTF-8编码的Base64字符串盐
     * @return UTF-8编码的Base64密文
     */
    public static String encrypt(String message, String salt) {
        byte[] saltArray = Base64.getDecoder().decode(salt.getBytes(JSONConfig.DEFAULT_CHARSET));
        byte[] messArray = message.getBytes(JSONConfig.DEFAULT_CHARSET);
        byte[] mixsArray = new byte[Math.max(saltArray.length, messArray.length)];
        for (int i = 0; i < mixsArray.length; i++) {
            byte s = i < saltArray.length?saltArray[i]:0;
            byte m = i < messArray.length?messArray[i]:0;
            mixsArray[i] = (byte)((s + m)/2);
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(mixsArray);
            return new String(Base64.getEncoder().encode(md.digest()), JSONConfig.DEFAULT_CHARSET);
        } catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        }
        return null;
    }
}