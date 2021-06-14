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
package org.gcszhn.system.service.docker;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class DockerExecConfig implements Serializable {
    /**序列化ID*/
    private static final long serialVersionUID = -4052728705754384469L;
    /**工作目录 */
    private @Getter String workingDir;
    /**工作用户 */
    private @Getter String username;
    /**工作环境变量 */
    private @Getter List<String> envs;
    /**是否以root权限运行 */
    private @Getter boolean privilege;
    /**是否启用TTY虚拟终端 */
    private @Getter boolean tty;
    /**标准输入流 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private transient @Getter InputStream inputStream;
    /**标准输出流 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private transient @Getter OutputStream outputStream;
    public DockerExecConfig withWorkingDir(String workingDir) {
        this.workingDir = workingDir;
        return this;
    }
    public DockerExecConfig withUser(String username) {
        this.username = username;
        return this;
    }
    public DockerExecConfig withEnvs(Collection<String> envs) {
        this.envs = new ArrayList<>(envs);
        return this;
    }
    public DockerExecConfig withEnvs(String... envs) {
        this.envs = Arrays.asList(envs);
        return this;
    }
    public DockerExecConfig withPrivilege(boolean privilege) {
        this.privilege = privilege;
        return this;
    }
    public DockerExecConfig withTty(boolean tty) {
        this.tty = tty;
        return this;
    }
    public DockerExecConfig withInputStream(InputStream inputStream) {
        this.inputStream= inputStream;
        return this;
    }
    public DockerExecConfig withOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }
}