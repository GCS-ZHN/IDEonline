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
package org.gcszhn.system.service.ssh;

import java.io.OutputStream;
import java.util.Properties;

import com.jcraft.jsch.Session;

/**
 * SSH远程连接服务
 * @author Zhang.H.N
 * @version 1.0
 */
public interface SSHService {
    public Session getSession(String username, String passwd, String host, 
    int port, int timeout, Properties sshConfig);
    public Session getConfigSession(int host);
    public void remoteExec(Session session, String cmds, OutputStream outputStream, OutputStream errStream);
}