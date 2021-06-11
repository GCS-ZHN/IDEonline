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
    private @Getter boolean privilage;
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
    public DockerExecConfig withPrivilage(boolean privilage) {
        this.privilage = privilage;
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