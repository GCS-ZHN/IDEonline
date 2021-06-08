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
package org.gcszhn;

import java.io.FileInputStream;

import javax.mail.Folder;

import org.gcszhn.system.config.JSONConfig;
import org.gcszhn.system.service.MailService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class mailServiceTest extends AppTest {
    @Autowired
    //@Qualifier("hutoolmail")
    MailService service;

    @Test
    public void sendEmailTest() throws Exception {
        try (FileInputStream fis = new FileInputStream("temp/mail.vm")) {
            String content = new String(fis.readAllBytes(), JSONConfig.DEFAULT_CHARSET);
            service.sendMail(
                "zhang.h.n@foxmail.com", 
                "IDEonline system update", 
                content, 
                "text/html;charset=utf-8"
                );
            Thread.sleep(10000);
        }
    }
    @Test
    public void readInBoxTest() {
        service.readInbox(Folder.READ_ONLY);
    }
}