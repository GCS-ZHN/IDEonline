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
package org.gcszhn.system.service.user;

import java.io.Closeable;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.user.User;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

/**
 * 用户任务实例
 * 
 * @author Zhang.H.N
 * @version 1.1
 */
@Data
public class UserJob implements Closeable, Serializable {
    /** 序列化ID */
    private static final long serialVersionUID = 6559107322050880917L;
    /** 任务ID */
    private String id;
    /** 创建任务时间 */
    private Date createdTime;
    /** 任务命令，第一个元素是命令，其它为命令参数 */
    private String[] cmds;
    /** 任务所有者 */
    private String account;
    /** 任务关联主机 */
    private int host;
    /** 任务命令 */
    @Deprecated
    private String cmd;
    /** 任务所有者 */
    @Deprecated
    private transient User user;
    /** 任务execID */
    private String execId;
    /** 任务标准输入 */
    @Deprecated
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private PipedInputStream stdin;
    /** 任务标准输入源 */
    @Deprecated
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Setter(AccessLevel.NONE)
    private PipedOutputStream stdinPipSource;
    /**任务是否关闭 */
    @Deprecated
    @Setter(AccessLevel.PRIVATE)
    private volatile boolean close;
    /** 任务标准输出 */
    private String stdoutfile;
    /** 任务超时 */
    private long timeout;
    /** 超时时间单位 */
    private TimeUnit timeOutUnit;
    /** 任务完成时的回调 */
    private Runnable onCommpleteCallback;
    public void setCmds(String... cmds) {
        this.cmds = cmds;
    }
    /**
     * 设置标准输入管道源
     * @param stdinPipSource 输入源，来自管道输出
     * @throws IOException 
     */
    @Deprecated
    public void setStdin(PipedOutputStream stdinPipSource) throws IOException {
        this.stdinPipSource = stdinPipSource;
        this.stdin = new PipedInputStream(stdinPipSource);
    }
    /**关闭输入流的源，同时向任务发送关闭信号 */
    @Deprecated
    public synchronized void close() throws IOException {
        close(true);
    }
    /**关闭输入流的源，同时选择是否向任务发送关闭信号 */
    @Deprecated
    public synchronized void close(boolean sendSignal) throws IOException {
        if (!isClose()) {
            if (sendSignal) {
                AppLog.printMessage("close job with sending signal");
                try {
                    stdinPipSource.write(3);//Ctrl +C
                    stdinPipSource.write(121);//y
                    stdinPipSource.write(10);//LF换行
                } catch (IOException e) {
                    if (!e.getMessage().equals("Pipe closed")) {
                        throw new IOException(e);
                    } else {
                        AppLog.printMessage(e.getMessage());
                    }
                }
            } else {
                AppLog.printMessage("close job without sending signal");
            }
            stdinPipSource.close();
            setClose(true);
        }
    }
}