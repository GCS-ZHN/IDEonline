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
package org.gcszhn.system.service.ssh.impl;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.cluster.ClusterService;
import org.gcszhn.system.service.ssh.SSHNode;
import org.gcszhn.system.service.ssh.SSHService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * SSH服务实现
 * 
 * @author Zhang.H.N
 * @version 1.0
 */
@Service
public class SSHServiceImpl implements SSHService {
    private Properties sshConfig = new Properties();
    @Autowired
    ClusterService clusterService;
    @Autowired
    public void setSshConfig(Environment env) {
        String[] keySet = { "StrictHostKeyChecking" };
        for (String key : keySet) {
            sshConfig.put(key, env.getProperty("ssh." + key));
        }
    }

    @Override
    public Session getSession(String username, String passwd, String host, int port, int timeout,
            Properties sshConfig) {
        JSch jSch = new JSch();
        Session session = null;
        try {
            session = jSch.getSession(username, host, port);
            session.setConfig(sshConfig);
            session.setPassword(passwd);
            session.connect(timeout);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            if (session != null) {
                try {
                    session.disconnect();
                } catch (Exception ex) {
                    AppLog.printMessage(null, ex, Level.ERROR);
                }
            }
        }
        return session;
    }
    @Override
    public Session getClusterSession(int host) {
        SSHNode sshNode = clusterService.getSshNodeByHost(host);
        if (sshNode==null) return null;
        return getSession(
            sshNode.getUser(), 
            sshNode.getAuth(),
            clusterService.getClusterDomain()+"."+host,
            sshNode.getPort(), 
            20000, this.sshConfig);
    }
    @Override
    public void remoteExec(Session session, String cmds, 
        OutputStream outputStream, OutputStream errStream) {
        ChannelExec channelExec = null;
        InputStream inputStream = null;
        InputStream errInputStream = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(cmds);
            channelExec.connect();
            inputStream = channelExec.getInputStream();
            errInputStream = channelExec.getErrStream();
            byte[] outbuffer = new byte[1024];
            byte[] errbuffer = new byte[1024];
            int outLen = 0, errLen = 0;
            while (outLen!=-1 || errLen!=-1) {
                outLen = inputStream.read(outbuffer);
                errLen = errInputStream.read(errbuffer);
                if (outLen > 0) outputStream.write(outbuffer, 0, outLen);
                if (errLen > 0) errStream.write(errbuffer, 0, errLen);
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } finally {
            close(inputStream);
            close(errInputStream);
            close(outputStream);
            if(outputStream!=errStream) close(errStream);
            if (channelExec != null) {
                try {
                    channelExec.disconnect();
                } catch (Exception e) {
                    AppLog.printMessage(null, e, Level.ERROR);
                }
            }
        }
    }
    private void close(Closeable closeable) {
        if (closeable==null||closeable==System.in||closeable==System.out
            ||closeable==System.err) return;
        try {
            closeable.close();
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
}