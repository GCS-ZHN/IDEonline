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

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

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
public class UserJob implements Serializable {
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
    /** 任务execID */
    private String execId;
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

    public void setCmds(String... cmds) {
        this.cmds = cmds;
    }
    /** 获取命令字符串 */
    public String getCmd() {
        return StringUtils.join(cmds, " ");
    }
}