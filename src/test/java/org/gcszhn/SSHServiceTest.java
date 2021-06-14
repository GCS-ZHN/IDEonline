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

import java.io.ByteArrayOutputStream;
import java.util.Properties;


import com.jcraft.jsch.Session;

import org.gcszhn.system.service.ssh.SSHService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SSHServiceTest extends AppTest {
    @Autowired
    SSHService sshService;
    @Test
    public void sshExecTest() {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        Properties sshConfig = new Properties(1);
        sshConfig.put("StrictHostKeyChecking", "no");
        Session session = sshService.getSession(
            "root", "idrb@sugon", "172.16.10.2", 22, 2000, sshConfig);
        sshService.remoteExec(session, "jpss -l", stdout, stderr);
        session.disconnect();
        System.out.print(stdout.toString());
        System.out.print(stderr.toString());
    }
    @Test
    public void sshClusterTest() {
        int[] hosts = {1,2,3,12,41,210};
        for (int host: hosts) {
            System.out.println("Test node " + host);
            Session session = sshService.getConfigSession(host);
            sshService.remoteExec(session, "which java", System.out, System.err);
            session.disconnect();
        }
    }
}